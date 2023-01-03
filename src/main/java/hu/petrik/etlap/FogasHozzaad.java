package hu.petrik.etlap;

import hu.petrik.etlap.database.Fogas;
import hu.petrik.etlap.database.FogasKategoria;
import hu.petrik.etlap.database.MenuDB;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

public class FogasHozzaad extends Controller {
    @javafx.fxml.FXML
    private TextField nev;
    @javafx.fxml.FXML
    private TextField leiras;
    @javafx.fxml.FXML
    private ChoiceBox<FogasKategoria> kategoriaValaszt;
    @javafx.fxml.FXML
    private Spinner<Integer> arSpinner;

    private MenuDB menuDB;

    @FXML
    private void init() {
        arSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 1, 1));
        Platform.runLater(() -> {
            try {
                menuDB = new MenuDB();
                List<FogasKategoria> categories = menuDB.getKategoriak();
                for (FogasKategoria category : categories) {
                    kategoriaValaszt.getItems().add(category);
                }
                kategoriaValaszt.setValue(categories.get(0));
            } catch (SQLException e) {
                hiba("Nem sikert csatlakozni az adatbázishoz", e.getMessage());
            }
        });
    }

    public void gombClick(ActionEvent actionEvent) {
        if (nev.getText().isEmpty() ||
                leiras.getText().isEmpty() ||
                kategoriaValaszt.getSelectionModel().isEmpty()) {
            hiba("Minden mező kitöltése kötelező");
            return;
        }

        String nev = this.nev.getText().trim();
        String leiras = this.leiras.getText().trim();
        int ar = arSpinner.getValueFactory().getValue();
        FogasKategoria kategoria = kategoriaValaszt.getSelectionModel().getSelectedItem();
        Fogas fogas = new Fogas(nev, kategoria, leiras, ar);
        try {
            if (menuDB.FogasHozzaad(fogas)) {
                informacio("Sikeres felvétel!");
                this.nev.setText("");
                this.leiras.setText("");
                arSpinner.getValueFactory().setValue(0);
                kategoriaValaszt.setValue(kategoriaValaszt.getItems().get(0));
            } else {
                hiba("Sikertelen felvétel", "ERROR 404");
            }
        } catch (SQLException e) {
            hiba("Nem sikert hozzá adni", e.getMessage());
        }
    }
}
