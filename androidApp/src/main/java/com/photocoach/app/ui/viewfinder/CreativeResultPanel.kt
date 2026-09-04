package com.photocoach.app.ui.viewfinder

import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import android.widget.ImageView
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.photocoach.app.CreativeResultUi
import com.photocoach.app.ViewfinderUi
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.creative.CreativeColorMatrix
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.ImageDecodePolicy
import com.photocoach.app.ui.theme.PhotoCoachTokens
import java.util.IdentityHashMap

@Composable
internal fun CreativeResultPanel(
    ui: ViewfinderUi,
    result: CreativeResultUi,
    onSelectPhoto: (String) -> Unit,
    onStyleChange: (CreativeStyle) -> Unit,
    onEdit: (EditAdjustment) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    onCompareOriginal: (Boolean) -> Unit,
    onSaveCopy: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onSharePhoto: (String) -> Unit,
    onFavoritePhoto: (String) -> Unit,
    onTrashPhoto: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    val colors = PhotoCoachTokens.colors
    val spacing = PhotoCoachTokens.spacing
    val radii = PhotoCoachTokens.radii
    val masks = PhotoCoachTokens.masks
    Box(modifier.background(colors.surfaceBase.copy(alpha = masks.modal)).testTag("creative_result")) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .background(colors.surfaceRaised, RoundedCornerShape(radii.card))
                .padding(spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (result.isBurst) "三张都已保留" else "原片已保留", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (result.isBurst) "推荐第 ${result.photos.first { it.id == result.recommendedId }.sequence} 张：${result.recommendationReason}；你可以改选"
                        else "可撤销轻编辑，只会另存副本",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp).testTag("creative_done")) { Text("完成") }
            }
            EditedPhotoPreview(
                uri = result.selectedPhoto.originalUri,
                style = if (result.compareOriginal) CreativeStyle.ORIGINAL else ui.creativeStyle,
                edit = if (result.compareOriginal) EditAdjustment(styleStrength = 0f) else result.edit,
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )
            if (result.selectedPhoto.beautyPreset != BeautyPreset.OFF) {
                Text("美颜·${result.selectedPhoto.beautyPreset.label}将在另存时应用；此处预览仅显示颜色编辑，对比原图为未美颜原片",
                    style = MaterialTheme.typography.labelSmall, modifier = Modifier.testTag("beauty_export_notice"))
            }
            if (result.photos.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    result.photos.forEach { photo ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectPhoto(photo.id) }
                                .border(
                                    2.dp,
                                    if (photo.id == result.selectedId) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp),
                                )
                                .padding(4.dp)
                                .testTag("burst_photo_${photo.sequence}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ResultThumbnail(photo.displayUri, Modifier.fillMaxWidth().height(72.dp))
                            Text(
                                buildString {
                                    append("第 ${photo.sequence} 张")
                                    if (photo.id == result.recommendedId) append(" · 推荐")
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
            Text(
                "风格与轻量编辑",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("creative_edit_section"),
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("风格", modifier = Modifier.width(56.dp))
                CreativeStyleControl(ui.creativeStyle, enabled = !result.exportInProgress, onStyleChange = onStyleChange)
                Spacer(Modifier.width(8.dp))
                Text("预览为近似效果，导出可能有细微差异", style = MaterialTheme.typography.labelSmall)
            }
            EditSlider("曝光", result.edit.exposureStops, EditAdjustment.MIN_EXPOSURE..EditAdjustment.MAX_EXPOSURE) {
                onEdit(result.edit.copy(exposureStops = it))
            }
            EditSlider("对比度", result.edit.contrast, EditAdjustment.MIN_CONTRAST..EditAdjustment.MAX_CONTRAST) {
                onEdit(result.edit.copy(contrast = it))
            }
            EditSlider("饱和度", result.edit.saturation, EditAdjustment.MIN_SATURATION..EditAdjustment.MAX_SATURATION) {
                onEdit(result.edit.copy(saturation = it))
            }
            EditSlider("色温", result.edit.temperature, EditAdjustment.MIN_TEMPERATURE..EditAdjustment.MAX_TEMPERATURE) {
                onEdit(result.edit.copy(temperature = it))
            }
            EditSlider("色调", result.edit.tint, EditAdjustment.MIN_TINT..EditAdjustment.MAX_TINT) {
                onEdit(result.edit.copy(tint = it))
            }
            EditSlider("褪色", result.edit.fade, EditAdjustment.MIN_FADE..EditAdjustment.MAX_FADE) {
                onEdit(result.edit.copy(fade = it))
            }
            EditSlider("强度", result.edit.styleStrength, 0f..1f) {
                onEdit(result.edit.copy(styleStrength = it))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onUndo, enabled = result.canUndo, modifier = Modifier.height(48.dp).testTag("creative_undo")) {
                    Text("撤销")
                }
                TextButton(onClick = onRedo, enabled = result.canRedo, modifier = Modifier.height(48.dp).testTag("creative_redo")) {
                    Text("重做")
                }
                TextButton(onClick = onReset, enabled = result.canReset, modifier = Modifier.height(48.dp).testTag("creative_reset")) {
                    Text("重置")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { onCompareOriginal(!result.compareOriginal) },
                    modifier = Modifier.height(48.dp).testTag("creative_compare_original"),
                ) { Text(if (result.compareOriginal) "查看效果" else "对比原图") }
                Button(
                    onClick = onSaveCopy,
                    enabled = !result.exportInProgress,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("creative_save_copy"),
                ) { Text(if (result.exportInProgress) "正在另存" else "另存副本") }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = masks.subtle))
            Text(
                "拍后操作",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("creative_post_actions_section"),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.space1)) {
                val uri = result.selectedPhoto.displayUri
                TextButton(onClick = { onOpenPhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_open")) { Text("打开") }
                TextButton(onClick = { onSharePhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_share")) { Text("分享") }
                TextButton(onClick = { onFavoritePhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_favorite")) { Text("收藏") }
                TextButton(onClick = { onTrashPhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_trash")) { Text("回收站") }
            }
            result.message?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("creative_message")) }
        }
    }
}

@Composable
private fun EditSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(56.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = range, modifier = Modifier.weight(1f))
        Text(formatSigned(value), modifier = Modifier.width(44.dp))
    }
}

@Composable
private fun EditedPhotoPreview(
    uri: String,
    style: CreativeStyle,
    edit: EditAdjustment,
    modifier: Modifier,
) {
    BoundedLocalImage(
        uri = uri,
        maximumPixels = ImageDecodePolicy.RESULT_PREVIEW_MAX_PIXELS,
        scaleType = ImageView.ScaleType.CENTER_INSIDE,
        contentDescription = "所选原片的编辑预览",
        colorMatrix = CreativeColorMatrix.forSelection(style, edit),
        errorLabel = "原片预览不可用",
        errorTag = "creative_preview_error",
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .testTag("creative_edit_preview"),
    )
}

@Composable
private fun ResultThumbnail(uri: String, modifier: Modifier) {
    BoundedLocalImage(
        uri = uri,
        maximumPixels = ImageDecodePolicy.THUMBNAIL_MAX_PIXELS,
        scaleType = ImageView.ScaleType.CENTER_CROP,
        contentDescription = "连拍照片缩略图",
        errorLabel = "缩略图不可用",
        errorTag = "burst_thumbnail_error",
        modifier = modifier.clip(RoundedCornerShape(6.dp)).background(Color.DarkGray),
    )
}

@Composable
internal fun BoundedLocalImage(
    uri: String,
    maximumPixels: Long,
    scaleType: ImageView.ScaleType,
    contentDescription: String,
    errorLabel: String,
    errorTag: String,
    modifier: Modifier,
    colorMatrix: FloatArray? = null,
) {
    var loadError by remember(uri, maximumPixels) { mutableStateOf<String?>(null) }
    var effectError by remember(uri) { mutableStateOf<String?>(null) }
    val imageControllers = remember { IdentityHashMap<ImageView, BoundedBitmapImageController>() }
    Box(modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                ImageView(context).also { image ->
                    imageControllers[image] = BoundedBitmapImageController(image)
                }
            },
            modifier = Modifier.fillMaxSize(),
            onReset = { image -> imageControllers[image]?.releaseImage() },
            onRelease = { image -> imageControllers.remove(image)?.releaseImage() },
            update = { image ->
                image.scaleType = scaleType
                image.contentDescription = contentDescription
                imageControllers.getValue(image).loadBounded(uri.toUri(), maximumPixels) { loadError = it }
                effectError = applyImageColorMatrix(image, colorMatrix)
            },
        )
        val visibleError = effectError ?: loadError
        visibleError?.let {
            Text(
                text = if (effectError != null) "效果不可用，已显示原图" else errorLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag(errorTag),
            )
        }
    }
}

private fun applyImageColorMatrix(image: ImageView, matrix: FloatArray?): String? = try {
    if (matrix == null || CreativeColorMatrix.isIdentity(matrix)) {
        image.clearColorFilter()
    } else {
        image.colorFilter = ColorMatrixColorFilter(AndroidColorMatrix(matrix))
    }
    null
} catch (error: Throwable) {
    runCatching { image.clearColorFilter() }
    error.message ?: "无法显示创意效果"
}

internal fun applyPreviewStyle(preview: PreviewView, style: CreativeStyle, strength: Float): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return if (style == CreativeStyle.ORIGINAL) null else "当前系统仅显示原图预览"
    }
    val matrix = CreativeColorMatrix.forSelection(style, EditAdjustment(styleStrength = strength))
    val applied = applyEffectWithOriginalFallback(
        applyEffect = {
            preview.setRenderEffect(
                if (CreativeColorMatrix.isIdentity(matrix)) null
                else RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(AndroidColorMatrix(matrix))),
            )
        },
        clearEffect = { preview.setRenderEffect(null) },
    )
    return if (applied) null else "创意预览效果不可用，已显示原图"
}

internal fun applyEffectWithOriginalFallback(
    applyEffect: () -> Unit,
    clearEffect: () -> Unit,
): Boolean = try {
    applyEffect()
    true
} catch (error: Throwable) {
    runCatching(clearEffect)
    false
}
