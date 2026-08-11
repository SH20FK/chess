package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Правила Игры", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_rules_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RuleCard(
                title = "1. Шестиугольная Доска (Hex Grid)",
                description = "Битва происходит на 6-стороннем поле из 91 гекс-ячейки для 3-х игроков (Розовый, Небесный, Мятный). Фигуры передвигаются по 6 ортогональным и 6 диагональным осям.",
                icon = Icons.Default.Hexagon
            )

            RuleCard(
                title = "2. Шоколадные Рогалики 🥐 (Пешки)",
                description = "Пешки сделаны в виде вкусных рогаликов с шоколадной начинкой! Они двигаются вперед по гексам в направлении центра и атакуют по диагональным лучам.",
                icon = Icons.Default.BakeryDining
            )

            RuleCard(
                title = "3. Ядерная Энергия и Взрывы ⚡",
                description = "Каждый ход, взятие вражеских фигур и контроль центральной желтой зоны даёт % ядерной энергии.",
                icon = Icons.Default.Bolt
            )

            RuleCard(
                title = "4. Тактическое Оружие 🚀",
                description = "• ЯДЕРНЫЙ УДАР (100%): Уничтожает гекс и 6 соседних клеток, превращая их в непроходимый кратер.\n• АВИАУДАР (50%): Бомбардирует всю горизонтальную гекс-линию.",
                icon = Icons.Default.Warning
            )

            RuleCard(
                title = "5. Победа 🏆",
                description = "Игрок, чей Король остается последним на доске, объявляется единым правителем трех держав!",
                icon = Icons.Default.EmojiEvents
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RuleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
