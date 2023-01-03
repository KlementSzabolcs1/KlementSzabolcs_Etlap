package hu.petrik.etlap;

import hu.petrik.etlap.database.Fogas;
import hu.petrik.etlap.database.FogasKategoria;
import hu.petrik.etlap.database.MenuDB;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuController extends Controller {

    @FXML
    private TabPane tabPane;
    @FXML
    private ListView<FogasKategoria> kategoriaListView;
    @FXML
    private GridPane kategoriaGridPane;
    @FXML
    private Spinner<Integer> arNovekedesSzazalekSpinner;
    @FXML
    private Spinner<Integer> arNovekedesPenzSpinner;
    @FXML
    private TableView<Fogas> menuTable;
    @FXML
    private TableColumn<Fogas, String> nevCol;
    @FXML
    private TableColumn<Fogas, String> kategoriaCol;
    @FXML
    private TableColumn<Fogas, Integer> arCol;
    @FXML
    private TextArea leiras;

    private MenuDB menuDB;
    private List<FogasKategoria> kivalasztottKategoria;

    @FXML
    private void init() {
        arNovekedesSzazalekSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 50, 0, 5));
        arNovekedesPenzSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 3000, 0, 50));
        nevCol.setCellValueFactory(new PropertyValueFactory<>("nev"));
        kategoriaCol.setCellValueFactory(new PropertyValueFactory<>("kategoria"));
        arCol.setCellValueFactory(new PropertyValueFactory<>("ar"));
        kivalasztottKategoria = new ArrayList<>();

        Platform.runLater(() -> {
            try {
                menuDB = new MenuDB();
                adatBetoltes();
            } catch (SQLException e) {
                hiba("Nem sikerült kapcsolódni a szerverre", e.getMessage());
                Platform.exit();
            }
        });
    }

    private void kategoriaFilter() throws SQLException {
        kategoriaGridPane.getChildren().clear();
        List<FogasKategoria> kategoria = menuDB.getKategoriak();
        int oszlopSzam = 0;
        int sorSzam = 0;
        for (FogasKategoria fogaskategoria : kategoria) {
            CheckBox checkBox = new CheckBox(fogaskategoria.getNev());

            checkBox.setOnAction(actionEvent -> {
                if (checkBox.isSelected()) {
                    kivalasztottKategoria.add(fogaskategoria);
                } else {
                    kivalasztottKategoria.remove(fogaskategoria);
                }
                try {
                    menuTabla();
                } catch (SQLException e) {
                    hiba("Hiba az adatbázis betöltése közben", e.getMessage());
                }
            });
            kategoriaGridPane.add(checkBox, oszlopSzam, sorSzam);
            if (oszlopSzam == 4) {
                sorSzam++;
                oszlopSzam = 0;
            } else {
                oszlopSzam++;
            }
        }
    }


    private void menuTabla() throws SQLException {
        menuTable.getItems().clear();
        leiras.setText("");
        List<Fogas> menu = menuDB.getSzurtMenu(kivalasztottKategoria);
        for (Fogas fogas : menu) {
            menuTable.getItems().add(fogas);
        }
    }

    private void kategoriaTable() throws SQLException {
        kategoriaListView.getItems().clear();
        List<FogasKategoria> fogasKategoria = menuDB.getKategoriak();
        for (FogasKategoria category : fogasKategoria) {
            kategoriaListView.getItems().add(category);
        }
    }

    private void stage(Stage stage) {
        tabPane.setDisable(true);
        stage.show();
        stage.setOnHidden(event -> {
            adatBetoltes();
            tabPane.setDisable(false);
        });
    }

    private void adatBetoltes() {
        try {
            menuTabla();
            kategoriaFilter();
            kategoriaTable();
        } catch (SQLException e) {
            hiba("Hiba az adatok betöltése közben", e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    public void hozzaadGombClick(ActionEvent actionEvent) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("menuhozzaadview.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 400);
            Stage stage = new Stage();
            stage.setTitle("Étel hozzáadás");
            stage.setScene(scene);
            stage(stage);
        } catch (IOException e) {
            hiba("Nem sikert betölteni", e.getMessage());
        }

    }

    @FXML
    public void eltavolitGombClick(ActionEvent actionEvent) {

        int selectedIndex = menuTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            figyelmeztetes("Előbb válassz ki egy elemet!");
            return;
        }

        Fogas kivalasztott = menuTable.getSelectionModel().getSelectedItem();
        if (elfogadas("Biztos törölni akarod a(z) " + kivalasztott.getNev() + " nevű ételt?")) {
            try {
                if (!menuDB.FogasTorles(kivalasztott.getId())) {
                    hiba("Sikertelen törlés");
                    return;
                }
                adatBetoltes();
            } catch (SQLException e) {
                hiba("Hiba a törlés közben", e.getMessage());
            }
        }
    }

    @FXML
    public void arNovekedesSzazalekClick(ActionEvent actionEvent) {
        int increment = arNovekedesSzazalekSpinner.getValueFactory().getValue();
        if (increment <= 0) {
            figyelmeztetes("Az ár emeléshez adjon meg egy érvényes értéket!");
            return;
        }

        int selectedIndex = menuTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            if (!elfogadas("Biztos szeretnéd növelni az összes termék árát?")) {
                return;
            }
            try {
                menuDB.arNovekedesSzazalekban(selectedIndex, increment);
            } catch (SQLException e) {
                hiba("Nem sikerült növelni", e.getMessage());
            }
        } else {
            Fogas selectedMeal = menuTable.getSelectionModel().getSelectedItem();
            if (!elfogadas("Biztos szeretnéd növelni a(z) " + selectedMeal.getNev() + " árát?")) {
                return;
            }
            try {
                menuDB.arNovekedesSzazalekban(selectedMeal.getId(), increment);
            } catch (SQLException e) {
                hiba("Nem sikerült növelni", e.getMessage());
            }

        }
        adatBetoltes();
    }


    @FXML
    public void arNovekedesPenzClick(ActionEvent actionEvent) {
        int arNovekedes = arNovekedesPenzSpinner.getValueFactory().getValue();
        if (arNovekedes <= 0) {
            figyelmeztetes("Az ár emeléshez adjon meg egy érvényes értéket!");
            return;
        }

        int kivalasztottIndex = menuTable.getSelectionModel().getSelectedIndex();
        if (kivalasztottIndex == -1) {
            if (!elfogadas("Biztos szeretnéd növelni az összes termék árát?")) {
                return;
            }
            try {
                menuDB.arNovekedesPenzben(kivalasztottIndex, arNovekedes);
            } catch (SQLException e) {
                hiba("Nem sikerült növelni", e.getMessage());
            }
        } else {
            Fogas kivalasztottFogas = menuTable.getSelectionModel().getSelectedItem();
            if (!elfogadas("Biztos szeretnéd növelni a(z) " + kivalasztottFogas.getNev() + " árát?")) {
                return;
            }
            try {
                menuDB.arNovekedesPenzben(kivalasztottFogas.getId(), arNovekedes);
            } catch (SQLException e) {
                hiba("Nem sikerült növelni", e.getMessage());
            }
        }
        adatBetoltes();
    }


    @FXML
    public void fogasKivalasztas(MouseEvent mouseEvent) {
        int kivalasztott = menuTable.getSelectionModel().getSelectedIndex();
        if (kivalasztott == -1) {
            leiras.setText("");
        } else {
            Fogas kivalasztottFogas = menuTable.getSelectionModel().getSelectedItem();
            leiras.setText(kivalasztottFogas.getLeiras());
        }
    }

    public void kategoriaHozzaadClick(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("kategoriahozzaadview.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Kategória hozzáadás");
            stage(stage);
        } catch (Exception e) {
            hiba("Hiba", e.getMessage());
        }
    }

    public void kategoriaTorlesClick(ActionEvent actionEvent) {
        int kivalasztott = kategoriaListView.getSelectionModel().getSelectedIndex();
        if (kivalasztott == -1) {
            figyelmeztetes("Nincs kiválasztva elem", "A törléshez előbb válasszon ki egy elemet");
            return;
        }

        FogasKategoria kategoria = kategoriaListView.getSelectionModel().getSelectedItem();

        if (!elfogadas("Biztos szeretnéd törölni a(z) " + kategoria.getNev() + " kategóriát?")) {
            return;
        }

        try {
            if (menuDB.kategoriaTorles(kategoria)) {
                informacio("Sikeres törlés");
            } else {
                figyelmeztetes("Sikertelen törlés");
            }
        } catch (SQLException e) {
            hiba("Sikertelen törlés", e.getMessage());
        }
        adatBetoltes();
    }
}