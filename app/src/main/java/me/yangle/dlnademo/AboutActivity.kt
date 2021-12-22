package me.yangle.dlnademo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import me.yangle.dlnademo.ui.theme.DLNADemoTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DLNADemoTheme {
                About()
            }
        }
    }

    @Composable
    private fun About() {
        val libs = Libs.Builder().withContext(LocalContext.current).build()
        val context = LocalContext.current

        LazyColumn {
            items(libs.libraries) {
                Card(
                    Modifier
                        .clickable {
                            val website = it.website?.replace(
                                "developer.android.com",
                                "developer.android.google.cn"
                            )
                            startActivity(
                                context,
                                Intent(Intent.ACTION_VIEW, Uri.parse(website)),
                                null
                            )
                        }
                        .fillParentMaxWidth()
                        .padding(8.dp),
                    RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colors.secondaryVariant)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Row(
                            Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OneLineText(
                                it.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.h6
                            )
                            it.artifactVersion?.let {
                                OneLineText(
                                    it,
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                        Row(Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                            it.developers.forEach {
                                it.name?.let {
                                    OneLineText(
                                        it,
                                        Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.subtitle1
                                    )
                                }
                            }
                        }
                        Divider()
                        OneLineText(
                            it.uniqueId,
                            Modifier.padding(8.dp)
                        )
                        it.description?.let { Text(it, Modifier.padding(8.dp)) }
                        Divider()
                        Row(Modifier.padding(8.dp)) {
                            it.licenses.forEach {
                                OneLineText(
                                    it.name,
                                    Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.body1,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun OneLineText(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign? = null,
        style: TextStyle = LocalTextStyle.current
    ) = Text(
        text,
        modifier,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = style
    )
}
