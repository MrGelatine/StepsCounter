package com.example.stepsapp

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object SharedData {

    var selectedSharedDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
    var date: LocalDate = LocalDate.now()
}