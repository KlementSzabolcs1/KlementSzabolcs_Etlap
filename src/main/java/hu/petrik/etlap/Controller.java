package hu.petrik.etlap;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public abstract class Controller {

    protected void hiba(String fejlec) {
        hiba(fejlec, "");
    }

    protected void hiba(String fejlec, String szoveg) {
        alert(Alert.AlertType.ERROR, fejlec, szoveg);
    }

    protected void informacio(String fejlec) {
        alert(Alert.AlertType.INFORMATION, fejlec, "");
    }

    protected boolean elfogadas(String fejlec) {
        boolean eredmeny = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(fejlec);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            eredmeny = true;
        }
        return eredmeny;
    }


    protected void figyelmeztetes(String headerText) {
        alert(Alert.AlertType.WARNING, headerText, "");
    }

    protected void figyelmeztetes(String fejlec, String szoveg) {
        alert(Alert.AlertType.WARNING, fejlec, szoveg);
    }

    protected void alert(Alert.AlertType alertType, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}