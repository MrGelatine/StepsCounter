package com.example.stepsapp.ui.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stepsapp.HealthConnectManager
import com.example.stepsapp.R
import com.example.stepsapp.SharedData
import com.example.stepsapp.SharedData.selectedSharedDay
import com.example.stepsapp.StepsTopAppBar
import com.example.stepsapp.ui.navigation.NavigationDestination
import com.example.stepsapp.viewmodel.AppViewModelProvider
import com.example.stepsapp.viewmodel.MainScreenViewModel
import com.example.stepsapp.viewmodel.RequestedRecord
import io.github.boguszpawlowski.composecalendar.SelectableWeekCalendar
import io.github.boguszpawlowski.composecalendar.rememberSelectableWeekCalendarState
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

/**
 * Entry route for Home screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToRecordEntry: () -> Unit
) {
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    val requestPermissions = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
        } else {
        }
    }


    val recordsUiState by viewModel.recordsUiState.collectAsState()

    val calendarState = rememberSelectableWeekCalendarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            StepsTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToRecordEntry,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.record_entry_title)
                )
            }
        },
    ) { innerPadding ->
        if(calendarState.selectionState.selection.size != 0){
            SharedData.date = calendarState.selectionState.selection[0]
        }
        viewModel.readDayRecords(requestPermissions)
        var l = recordsUiState.recordsList
        Column(modifier = Modifier
            .padding(innerPadding)) {
            SelectableWeekCalendar(calendarState = calendarState)

            HomeBody(
                modifier = Modifier
                    .fillMaxSize(),
                itemList = recordsUiState.recordsList,
                requestedRecord = viewModel.requestedRecordState,
                updateDate = viewModel::updateRequestedRecordState,
                deleteDate = viewModel::deleteRecord
            )

        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeBody(
    modifier: Modifier = Modifier,
    itemList: List<StepsRecord>,
    requestedRecord: RequestedRecord,
    updateDate: (RequestedRecord) -> Unit,
    deleteDate: (ZonedDateTime, ZonedDateTime) -> Unit
) {
    val mContext = LocalContext.current

    val mCalendar = Calendar.getInstance()
    val mYearPick = mCalendar.get(Calendar.YEAR)
    val mMonthPick = mCalendar.get(Calendar.MONTH)
    val mDayPick = mCalendar.get(Calendar.DAY_OF_MONTH)

    val selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var recordDate by remember { mutableStateOf(
        requestedRecord.selectedDay.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))) }

    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            selectedDate.set(mYear, mMonth, mDayOfMonth)

            val newDate = ZonedDateTime.ofInstant(selectedDate.toInstant(), ZoneId.systemDefault())
            selectedSharedDay = newDate
            updateDate(requestedRecord.copy(selectedDay = newDate.truncatedTo(ChronoUnit.DAYS)))
            recordDate = newDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        }, mYearPick, mMonthPick, mDayPick
    )
    mDatePickerDialog.datePicker.maxDate = mCalendar.timeInMillis

    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (itemList.isEmpty()){
            Text(
                text = "No records",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }
        else {
            Text(
                text = "Total: ${requestedRecord.stepsOverall}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            RecordsList(
                itemList = itemList,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small)),
                onDelete = deleteDate
            )
        }
    }
}

@Composable
private fun RecordsList(
    itemList: List<StepsRecord>, modifier: Modifier = Modifier,
    onDelete: (ZonedDateTime, ZonedDateTime) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(itemList.size) { id ->
            RecordItem(item = itemList[id], modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small)),
                deleteRecord = onDelete)
        }
    }
}

@Composable
private fun RecordItem(item: StepsRecord, modifier: Modifier = Modifier,
                       deleteRecord: (ZonedDateTime, ZonedDateTime) -> Unit)
{
    Log.d("test-item", "${item.startZoneOffset} || ${item.count}")
    val startTime = ZonedDateTime.ofInstant(item.startTime, item.startZoneOffset)
    val endTime = ZonedDateTime.ofInstant(item.endTime, item.endZoneOffset)
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column (
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ){
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${if(startTime.hour / 10 == 0) "0${startTime.hour}" else startTime.hour}:" +
                        "${if(startTime.minute / 10 == 0) "0${startTime.minute}" else startTime.minute} - " +
                        "${if(endTime.hour / 10 == 0) "0${endTime.hour}" else endTime.hour}:" +
                        "${if(endTime.minute / 10 == 0) "0${endTime.minute}" else endTime.minute}",
                    style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${item.count} steps", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {deleteRecord(startTime, endTime)},
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_delete_24),
                        contentDescription = "delete record"
                    )
                }
            }
        }
    }
}
