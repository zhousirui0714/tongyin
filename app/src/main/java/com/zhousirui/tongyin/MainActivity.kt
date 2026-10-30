package com.zhousirui.tongyin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/* ---------- 配色 ---------- */
private val Ink = Color(0xFF0E1116)
private val Panel = Color(0xFF161C24)
private val Accent = Color(0xFF3FB6A8)
private val TextMain = Color(0xFFE8EDF2)
private val TextDim = Color(0xFF7E8B9A)
private val KeyTop = Color(0xFFF4F7FA)
private val KeyBottom = Color(0xFFD3DBE4)
private val KeyInk = Color(0xFF2A3340)

private data class Note(val name: String, val frequency: Double)

/** C 大调一个八度，够用来判断音高和音色 */
private val NOTES = listOf(
    Note("C4", 261.63),
    Note("D4", 293.66),
    Note("E4", 329.63),
    Note("F4", 349.23),
    Note("G4", 392.00),
    Note("A4", 440.00),
    Note("B4", 493.88),
    Note("C5", 523.25)
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Accent,
                    onPrimary = Ink,
                    background = Ink,
                    onBackground = TextMain,
                    surface = Panel,
                    onSurface = TextMain
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    TongYinScreen()
                }
            }
        }
    }
}

@Composable
private fun TongYinScreen() {
    val engine = remember { ToneEngine() }
    var lastNote by remember { mutableStateOf("还没有按过琴键") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Header()

        TrialPanel(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onPlay = { note ->
                engine.play(note.frequency)
                lastNote = "${note.name}　" + String.format(Locale.US, "%.2f", note.frequency) + " Hz"
            }
        )

        Footer(lastNote)
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "同音",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                letterSpacing = 8.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "TONGYIN  ·  MIDI SEQUENCER",
                fontSize = 12.sp,
                color = Accent,
                letterSpacing = 3.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("v0.1 · 骨架版", fontSize = 13.sp, color = TextMain)
            Spacer(Modifier.height(5.dp))
            Text("48000 Hz · 单声道 · 纯 Kotlin 合成", fontSize = 12.sp, color = TextDim)
        }
    }
}

@Composable
private fun TrialPanel(modifier: Modifier = Modifier, onPlay: (Note) -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Panel)
            .padding(28.dp)
    ) {
        Text("试听", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "按下琴键，确认这台平板能出声。这一版还没有音序器，只是把音频通路先打通。",
            fontSize = 13.sp,
            color = TextDim
        )
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NOTES.forEach { note ->
                PianoKey(
                    note = note,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    onPlay(note)
                }
            }
        }
    }
}

@Composable
private fun PianoKey(note: Note, modifier: Modifier = Modifier, onPlay: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (pressed) {
                    Brush.verticalGradient(listOf(Accent, Accent))
                } else {
                    Brush.verticalGradient(listOf(KeyTop, KeyBottom))
                }
            )
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onPlay() }
            .padding(bottom = 18.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = note.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (pressed) Ink else KeyInk
        )
    }
}

@Composable
private fun Footer(lastNote: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(lastNote, fontSize = 13.sp, color = TextDim)
        Text("下一步：钢琴卷帘 · 16 轨 · 128 音色", fontSize = 13.sp, color = Accent)
    }
}
