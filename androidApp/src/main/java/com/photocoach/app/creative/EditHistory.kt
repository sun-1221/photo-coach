package com.photocoach.app.creative

class EditHistory(
    initial: EditAdjustment = EditAdjustment(),
    private val maximumUndoSteps: Int = 32,
) {
    private val initialState = initial.normalized()
    private val undoStack = ArrayDeque<EditAdjustment>()
    private val redoStack = ArrayDeque<EditAdjustment>()
    private var lastChangedField: EditField? = null

    var current: EditAdjustment = initialState
        private set

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val canReset: Boolean get() = current != initialState

    fun update(next: EditAdjustment): EditAdjustment {
        val normalized = next.normalized()
        if (normalized == current) return current
        val changedField = current.singleChangedField(normalized)
        if (changedField == null || changedField != lastChangedField) {
            undoStack.addLast(current)
            while (undoStack.size > maximumUndoSteps) undoStack.removeFirst()
        }
        current = normalized
        redoStack.clear()
        lastChangedField = changedField
        return current
    }

    fun undo(): EditAdjustment {
        if (undoStack.isNotEmpty()) {
            redoStack.addLast(current)
            current = undoStack.removeLast()
        }
        lastChangedField = null
        return current
    }

    fun redo(): EditAdjustment {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(current)
            current = redoStack.removeLast()
        }
        lastChangedField = null
        return current
    }

    fun reset(): EditAdjustment {
        if (current != initialState) {
            undoStack.addLast(current)
            while (undoStack.size > maximumUndoSteps) undoStack.removeFirst()
            current = initialState
        }
        redoStack.clear()
        lastChangedField = null
        return current
    }

    fun clear(initial: EditAdjustment = EditAdjustment()): EditHistory =
        EditHistory(initial, maximumUndoSteps)
}

private enum class EditField {
    EXPOSURE,
    CONTRAST,
    SATURATION,
    TEMPERATURE,
    TINT,
    FADE,
    STYLE_STRENGTH,
}

private fun EditAdjustment.singleChangedField(other: EditAdjustment): EditField? {
    val changed = buildList {
        if (exposureStops != other.exposureStops) add(EditField.EXPOSURE)
        if (contrast != other.contrast) add(EditField.CONTRAST)
        if (saturation != other.saturation) add(EditField.SATURATION)
        if (temperature != other.temperature) add(EditField.TEMPERATURE)
        if (tint != other.tint) add(EditField.TINT)
        if (fade != other.fade) add(EditField.FADE)
        if (styleStrength != other.styleStrength) add(EditField.STYLE_STRENGTH)
    }
    return changed.singleOrNull()
}
