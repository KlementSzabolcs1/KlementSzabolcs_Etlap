package hu.petrik.etlap;

import hu.petrik.etlap.database.FogasKategoria;
import hu.petrik.etlap.database.MenuDB;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class KategoriaHozzaadController extends Controller {
    public TextField textField;
    private MenuDB menuDB;


    @FXML
    private void init() {
        Platform.runLater(()-> {
            try {
                menuDB = new MenuDB();
            } catch (SQLException e) {
                hiba("Nem sikerült csatlakozi az adatbázishoz", e.getMessage());
            }
        });
    }

    public void kategoriaClick(ActionEvent actionEvent) {
        String kategoriaNev = textField.getText().trim().toLowerCase();
        if (kategoriaNev.isEmpty()) {
            figyelmeztetes("A mező kitöltése kötelező!");
            return;
        }
        FogasKategoria category = new FogasKategoria(kategoriaNev);
        try {
            menuDB.kategoriaHozzaad(category);
            Stage stage = (Stage) textField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                hiba("Ilyen kategória már létezik");
                return;
            }
            hiba("Nem sikerült felvenni a kategóriát", String.valueOf(e.getErrorCode()));
        }
    }
}
