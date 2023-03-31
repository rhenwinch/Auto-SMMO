package com.xcape.simplemmomod.ui.account_picker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.xcape.simplemmomod.common.Endpoints
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.ui.common.BasicTopAppBar
import com.xcape.simplemmomod.ui.common.IconResource
import com.xcape.simplemmomod.ui.theme.SimpleMMOModTheme
import dagger.hilt.android.AndroidEntryPoint

const val USER_DATA = "Cookie"

@AndroidEntryPoint
class AccountPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val closeActivity = { cookie: String? ->
                val intent = this.intent
                intent.putExtra(USER_DATA, cookie)
                setResult(RESULT_OK, intent)
                this.finish()
            }

            SimpleMMOModTheme {
                AccountPickerUi(activityKiller = closeActivity)
            }
        }
    }
}

@Composable
fun AccountPickerUi(
    activityKiller: (String?) -> Unit,
    accountPickerViewModel: AccountPickerViewModel = viewModel()
) {
    val previousLoggedInUsers = accountPickerViewModel.users.collectAsState(initial = emptyList())

    Surface {
        Scaffold(
            topBar = {
                BasicTopAppBar(
                    header = "Choose an Account",
                    onNavigationIconClick = { activityKiller(null) }
                )
            }
        ) { scaffoldPadding ->
            LazyColumn(modifier = Modifier.padding(scaffoldPadding)) {
                items(previousLoggedInUsers.value) { user ->
                    LoadAccount(user = user, onAccountClick = activityKiller)
                }
            }
        }
    }
}

@Composable
fun LoadAccount(user: User, onAccountClick: (String?) -> Unit) {
    Card(
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier.padding(10.dp)
            .clickable {
                onAccountClick(user.cookie)
            }
    ) {
        Row {
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val (image, userInfo, userProgress, playButton) = createRefs()

                AsyncImage(
                    model = Endpoints.BASE_URL + user.avatar,
                    contentDescription = user.username,
                    placeholder = IconResource.fromImageVector(Icons.Default.Person).asPainterResource(),
                    modifier = Modifier
                        .padding(15.dp)
                        .clip(CircleShape)
                        .size(45.dp)
                        .constrainAs(image) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .constrainAs(userInfo) {
                            start.linkTo(image.end, margin = 5.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Text(
                        text = user.username,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    Text(
                        text = "#${user.id}",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .constrainAs(userProgress) {
                            start.linkTo(userInfo.end, margin = 25.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = "Level: ",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Text(
                            text = String.format("%,d", user.level),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 0.dp)
                    ) {
                        Text(
                            text = "Gold: ",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Text(
                            text = String.format("%,d", user.gold),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Use Account Button",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .scale(1.1F)
                        .constrainAs(playButton) {
                            end.linkTo(parent.end, margin = 15.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                )
            }
        }
    }
}

@Preview
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun PreviewApp() {
    SimpleMMOModTheme {
        Scaffold {
            LoadAccount(
                user = User(
                    username = "Ameifire",
                    id = 974125,
                    gold = 1056156,
                    level = 45645,
                    avatar = "/img/sprites/20.png"
                )
            ) {}
        }
    }
}