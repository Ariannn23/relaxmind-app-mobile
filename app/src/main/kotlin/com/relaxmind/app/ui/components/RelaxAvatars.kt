package com.relaxmind.app.ui.components

import androidx.annotation.DrawableRes
import com.relaxmind.app.R

data class RelaxAvatar(
    val url: String,
    @DrawableRes val drawableRes: Int
)

val LocalRelaxAvatars = listOf(
    RelaxAvatar("relaxmind://avatar/buho", R.drawable.avatar_buho),
    RelaxAvatar("relaxmind://avatar/chia_agua", R.drawable.avatar_chia_agua),
    RelaxAvatar("relaxmind://avatar/chica_hada", R.drawable.avatar_chica_hada),
    RelaxAvatar("relaxmind://avatar/chica_luna", R.drawable.avatar_chica_luna),
    RelaxAvatar("relaxmind://avatar/chica_morado", R.drawable.avatar_chica_morado),
    RelaxAvatar("relaxmind://avatar/chico_celeste", R.drawable.avatar_chico_celeste),
    RelaxAvatar("relaxmind://avatar/chico_verde", R.drawable.avatar_chico_verde),
    RelaxAvatar("relaxmind://avatar/conejo", R.drawable.avatar_conejo),
    RelaxAvatar("relaxmind://avatar/dragon", R.drawable.avatar_dragon),
    RelaxAvatar("relaxmind://avatar/gato", R.drawable.avatar_gato),
    RelaxAvatar("relaxmind://avatar/koala", R.drawable.avatar_koala),
    RelaxAvatar("relaxmind://avatar/oso", R.drawable.avatar_oso),
    RelaxAvatar("relaxmind://avatar/panda", R.drawable.avatar_panda),
    RelaxAvatar("relaxmind://avatar/zorro", R.drawable.avatar_zorro)
)

fun getAvatarDrawableRes(url: String): Int {
    return LocalRelaxAvatars.find { it.url == url }?.drawableRes ?: R.drawable.avatar
}
