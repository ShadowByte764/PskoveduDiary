package ru.pskovedu.diary

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Modern Deep Violet Color Palette
val VioletBackground = Color(0xFF120824)
val VioletSurface = Color(0xFF1E0F38)
val VioletCard = Color(0xFF2B164C)
val VioletAccent = Color(0xFF9D4EDD)
val VioletLightAccent = Color(0xFFC77DFF)
val NeonCyan = Color(0xFF00F5FF)

val Grade5Color = Color(0xFF00E676)
val Grade4Color = Color(0xFF29B6F6)
val Grade3Color = Color(0xFFFFCA28)
val Grade2Color = Color(0xFFFF5252)

data class Subject(
    val id: Int,
    val name: String,
    val teacher: String,
    val grades: MutableList<Int>
) {
    val averageScore: Double
        get() = if (grades.isEmpty()) 0.0 else grades.average()
}

data class LessonItem(
    val name: String,
    val time: String,
    val room: String,
    val homework: String,
    val grade: Int?
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val isNew: Boolean
)

class MainActivity : ComponentActivity() {

    private val esiaAuthUrl = "https://esia.gosuslugi.ru/aas/oauth2/ac?client_id=PSKOVEDU_DIARY_APP&redirect_uri=https://pskovedu.ru/auth/callback&response_type=code&scope=openid"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        setContent {
            PskoveduTheme {
                MainAppStructure(
                    onAuthGosuslugi = { openGosuslugiAuth() },
                    onSendNotification = { title, msg -> showLocalNotification(title, msg) }
                )
            }
        }
    }

    private fun openGosuslugiAuth() {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, Uri.parse(esiaAuthUrl))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "GRADES_CHANNEL",
                "Уведомления об оценках",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о новых оценках в дневнике Псковеду"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showLocalNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = androidx.core.app.NotificationCompat.Builder(this, "GRADES_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}

@Composable
fun PskoveduTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = VioletBackground,
            surface = VioletSurface,
            primary = VioletAccent,
            secondary = NeonCyan
        ),
        content = content
    )
}

@Composable
fun MainAppStructure(
    onAuthGosuslugi: () -> Unit,
    onSendNotification: (String, String) -> Unit
) {
    var isLoggedIn by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginGosuslugi = {
                onAuthGosuslugi()
                // Simulating login completion for demo inside app
                isLoggedIn = true
            }
        )
    } else {
        MainDashboardScreen(onSendNotification = onSendNotification)
    }
}

@Composable
fun LoginScreen(onLoginGosuslugi: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VioletBackground, Color(0xFF0A0414))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = VioletSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(VioletAccent, NeonCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ПСKOВЕДУ",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Электронный дневник Псковской области",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                )

                Button(
                    onClick = onLoginGosuslugi,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4CD3)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Войти через Госуслуги (ЕСИА)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(onSendNotification: (String, String) -> Unit) {
    var currentTab by remember { mutableIntStateOf(1) } // 0: Schedule, 1: Grades, 2: Notifications

    val subjectsState = remember {
        mutableStateListOf(
            Subject(1, "Алгебра", "Иванова О.В.", mutableListOf(5, 4, 3, 5, 4)),
            Subject(2, "Русский язык", "Петрова Е.Н.", mutableListOf(4, 4, 5, 4, 3)),
            Subject(3, "Физика", "Сидоров С.М.", mutableListOf(3, 4, 3, 4)),
            Subject(4, "История", "Васильев А.П.", mutableListOf(5, 5, 4, 5)),
            Subject(5, "Литература", "Николаева Т.И.", mutableListOf(5, 4, 5, 5)),
            Subject(6, "География", "Кузнецов Д.В.", mutableListOf(4, 3, 4, 4))
        )
    }

    val notificationsState = remember {
        mutableStateListOf(
            NotificationItem(1, "Новая оценка по Алгебре", "Учитель поставил 5 за контрольную работу", "10:15", true),
            NotificationItem(2, "Домашнее задание", "Добавлено новое Д/З по Физике", "Вчера", false)
        )
    }

    var selectedSubjectForCalc by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Псковеду • Дневник",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VioletBackground)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = VioletSurface) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Schedule") },
                    label = { Text("Расписание") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Grades") },
                    label = { Text("Оценки") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Уведомления") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = VioletBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                0 -> ScheduleView()
                1 -> GradesView(
                    subjects = subjectsState,
                    onOpenCalculator = { subject -> selectedSubjectForCalc = subject }
                )
                2 -> NotificationsView(
                    notifications = notificationsState,
                    onSimulateNewGrade = {
                        val newGrade = 5
                        val subjectName = "Алгебра"
                        val title = "Новая оценка!"
                        val msg = "По предмету $subjectName получена оценка $newGrade"

                        notificationsState.add(
                            0,
                            NotificationItem(
                                id = System.currentTimeMillis().toInt(),
                                title = title,
                                message = msg,
                                time = "Только что",
                                isNew = true
                            )
                        )
                        subjectsState.find { it.name == subjectName }?.grades?.add(newGrade)
                        onSendNotification(title, msg)
                    }
                )
            }

            selectedSubjectForCalc?.let { subject ->
                GradeCalculatorDialog(
                    subject = subject,
                    onDismiss = { selectedSubjectForCalc = null }
                )
            }
        }
    }
}

@Composable
fun ScheduleView() {
    val lessons = listOf(
        LessonItem("Алгебра", "08:30 - 09:15", "Каб. 304", "Стр. 142, №412, 415", 5),
        LessonItem("Русский язык", "09:25 - 10:10", "Каб. 210", "Упражнение 204", 4),
        LessonItem("Физика", "10:25 - 11:10", "Каб. 402", "Параграф 18, конспект", null),
        LessonItem("История", "11:30 - 12:15", "Каб. 105", "Читать главу 4", 5),
        LessonItem("Литература", "12:25 - 13:10", "Каб. 210", "Выучить стих", null)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(lessons) { lesson ->
            Card(
                colors = CardDefaults.cardColors(containerColor = VioletCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = lesson.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "${lesson.time} • ${lesson.room}", fontSize = 12.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Д/З: ${lesson.homework}", fontSize = 13.sp, color = VioletLightAccent)
                    }

                    lesson.grade?.let { g ->
                        GradeBadge(grade = g, size = 38)
                    }
                }
            }
        }
    }
}

@Composable
fun GradesView(
    subjects: List<Subject>,
    onOpenCalculator: (Subject) -> Unit
) {
    val overallAverage = if (subjects.isNotEmpty()) subjects.map { it.averageScore }.average() else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VioletSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Общий средний балл", fontSize = 14.sp, color = Color.LightGray)
                        Text(
                            text = String.format("%.2f", overallAverage),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(VioletAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        items(subjects) { subject ->
            Card(
                colors = CardDefaults.cardColors(containerColor = VioletCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = subject.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = subject.teacher, fontSize = 12.sp, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%.2f", subject.averageScore),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = getScoreColor(subject.averageScore),
                                modifier = Modifier.padding(end = 12.dp)
                            )

                            IconButton(
                                onClick = { onOpenCalculator(subject) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VioletSurface)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Calculator",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        subject.grades.forEach { g ->
                            GradeBadge(grade = g, size = 30)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradeCalculatorDialog(
    subject: Subject,
    onDismiss: () -> Unit
) {
    val addedGrades = remember { mutableStateListOf<Int>() }
    val allGrades = remember(addedGrades.size) { subject.grades + addedGrades }
    val newAverage = if (allGrades.isNotEmpty()) allGrades.average() else 0.0

    // Calculate how many 5s needed for target 4.5
    val currentSum = subject.grades.sum() + addedGrades.sum()
    val currentCount = subject.grades.size + addedGrades.size
    val neededFives = calculateNeededFives(currentSum, currentCount, 4.5)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VioletSurface,
        title = {
            Text(text = "Прогноз: ${subject.name}", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(text = "Текущий балл: ${String.format("%.2f", subject.averageScore)}", color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = VioletCard),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Прогнозируемый балл", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = String.format("%.2f", newAverage),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = getScoreColor(newAverage)
                        )
                        if (neededFives > 0) {
                            Text(
                                text = "До итоговой «5» нужно ещё $neededFives пятёрок подряд",
                                fontSize = 12.sp,
                                color = VioletLightAccent,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = "Итоговая «5» уже выходит! 🎉",
                                fontSize = 12.sp,
                                color = Grade5Color,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Добавьте гипотетические оценки:", fontSize = 13.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(5, 4, 3, 2).forEach { g ->
                        Button(
                            onClick = { addedGrades.add(g) },
                            colors = ButtonDefaults.buttonColors(containerColor = getGradeColor(g)),
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "+$g", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                if (addedGrades.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Добавлено: ${addedGrades.joinToString(", ")}", fontSize = 12.sp, color = Color.LightGray)
                        TextTextButton(onClick = { addedGrades.clear() }) {
                            Text(text = "Сброс", color = Grade2Color, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = VioletAccent)) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun NotificationsView(
    notifications: List<NotificationItem>,
    onSimulateNewGrade: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onSimulateNewGrade,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AddAlert, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Симулировать новую оценку (PUSH)", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notifications) { notif ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = VioletCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (notif.isNew) NeonCyan else Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = VioletBackground)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = notif.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(text = notif.message, color = Color.LightGray, fontSize = 13.sp)
                        }

                        Text(text = notif.time, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GradeBadge(grade: Int, size: Int = 32) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(getGradeColor(grade)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = grade.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2.2).sp
        )
    }
}

fun getGradeColor(grade: Int): Color = when (grade) {
    5 -> Grade5Color
    4 -> Grade4Color
    3 -> Grade3Color
    else -> Grade2Color
}

fun getScoreColor(score: Double): Color = when {
    score >= 4.5 -> Grade5Color
    score >= 3.5 -> Grade4Color
    score >= 2.5 -> Grade3Color
    else -> Grade2Color
}

fun calculateNeededFives(currentSum: Int, currentCount: Int, targetAvg: Double): Int {
    var fivesNeeded = 0
    var sum = currentSum.toDouble()
    var count = currentCount.toDouble()

    while (count > 0 && (sum / count) < targetAvg && fivesNeeded < 50) {
        sum += 5
        count += 1
        fivesNeeded++
    }
    return fivesNeeded
}
