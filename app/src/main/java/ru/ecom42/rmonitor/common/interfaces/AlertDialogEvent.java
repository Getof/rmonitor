package ru.ecom42.rmonitor.common.interfaces;

import ru.ecom42.rmonitor.common.utils.AlertDialogBuilder;

public interface AlertDialogEvent {
    void onAnswerDialog(AlertDialogBuilder.DialogResult result);
}
