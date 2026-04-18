package com.lawrence.justsitting

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "The Essence of JustSitting",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Unlike most meditation apps that focus on mundane benefits—such as stress reduction or better sleep—JustSitting is exclusively dedicated to the ultimate purpose: recognizing the true nature of reality.",
            fontSize = 16.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Simplicity and Authenticity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "This app eliminates unnecessary distractions, providing a growing foundation of Authentic Teachings from traditional lineages (Shamatha, Vipashyana, Madhyamaka, Dzogchen, and Mahamudra). It is not a relaxation tool, but a support for profound practice.",
            fontSize = 16.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Essential Disclaimer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF44336)
        )
        Text(
            text = "While this app provides precious instructions, finding an Authentic Teacher who can provide correct explanations, Transmissions, and Empowerments is indispensable on this path. Such a master is \"rarer than the edelweiss flower,\" and JustSitting aims to be the best aid in preparing you for this crucial meeting.",
            fontSize = 16.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Donation Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Support the Project",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "JustSitting is and will remain a free app. Your donation helps in developing and expanding the database of authentic teachings.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/LaurentiuGrecu09"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
                ) {
                    Text("Support the Dhamma Path", color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}