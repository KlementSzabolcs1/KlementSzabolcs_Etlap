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
                hiba("Nem siker??lt kapcsol??dni a szerverre", e.getMessage());
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
                    hiba("Hiba az adatb??zis bet??lt??se k??zben", e.getMessage());
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
            hiba("Hiba az adatok bet??lt??se k??zben", e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    public void hozzaadGombClick(ActionEvent actionEvent) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("menuhozzaadview.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 400);
            Stage stage = new Stage();
            stage.setTitle("??tel hozz??ad??s");
            stage.setScene(scene);
            stage(stage);
        } catch (IOException e) {
            hiba("Nem sikert bet??lteni", e.getMessage());
        }

    }

    @FXML
    public void eltavolitGombClick(ActionEvent actionEvent) {

        int selectedIndex = menuTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            figyelmeztetes("El??bb v??lassz ki egy elemet!");
            return;
        }

        Fogas kivalasztott = menuTable.getSelectionModel().getSelectedItem();
        if (elfogadas("Biztos t??r??lni akarod a(z) " + kivalasztott.getNev() + " nev?? ??telt?")) {
            try {
                if (!menuDB.FogasTorles(kivalasztott.getId())) {
                    hiba("Sikertelen t??rl??s");
                    return;
                }
                adatBetoltes();
            } catch (SQLException e) {
                hiba("Hiba a t??rl??s k??zben", e.getMessage());
            }
        }
    }

    @FXML
    public void arNovekedesSzazalekClick(ActionEvent actionEvent) {
        int increment = arNovekedesSzazalekSpinner.getValueFactory().getValue();
        if (increment <= 0) {
            figyelmeztetes("Az ??r emel??shez adjon meg egy ??rv??nyes ??rt??ket!");
            return;
        }

        int selectedIndex = menuTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            if (!elfogadas("Biztos szeretn??d n??velni az ??sszes term??k ??r??t?")) {
                return;
            }
            try {
                menuDB.arNovekedesSzazalekban(selectedIndex, increment);
            } catch (SQLException e) {
                hiba("Nem siker??lt n??velni", e.getMessage());
            }
        } else {
            Fogas selectedMeal = menuTable.getSelectionModel().getSelectedItem();
            if (!elfogadas("Biztos szeretn??d n??velni a(z) " + selectedMeal.getNev() + " ??r??t?")) {
                return;
            }
            try {
                menuDB.arNovekedesSzazalekban(selectedMeal.getId(), increment);
            } catch (SQLException e) {
                hiba("Nem siker??lt n??velni", e.getMessage());
            }

        }
        adatBetoltes();
    }


    @FXML
    public void arNovekedesPenzClick(ActionEvent actionEvent) {
        int arNovekedes = arNovekedesPenzSpinner.getValueFactory().getValue();
        if (arNovekedes <= 0) {
            figyelmeztetes("Az ??r emel??shez adjon meg egy ??rv??nyes ??rt??ket!");
            return;
        }

        int kivalasztottIndex = menuTable.getSelectionModel().getSelectedIndex();
        if (kivalasztottIndex == -1) {
            if (!elfogadas("Biztos szeretn??d n??velni az ??sszes term??k ??r??t?")) {
                return;
            }
            try {
                menuDB.arNovekedesPenzben(kivalasztottIndex, arNovekedes);
            } catch (SQLException e) {
                hiba("Nem siker??lt n??velni", e.getMessage());
            }
        } else {
            Fogas kivalasztottFogas = menuTable.getSelectionModel().getSelectedItem();
            if (!elfogadas("Biztos szeretn??d n??velni a(z) " + kivalasztottFogas.getNev() + " ??r??t?")) {
                return;
            }
            try {
                menuDB.arNovekedesPenzben(kivalasztottFogas.getId(), arNovekedes);
            } catch (SQLException e) {
                hiba("Nem siker??lt n??velni", e.getMessage());
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
            stage.setTitle("Kateg??ria hozz??ad??s");
            stage(stage);
        } catch (Exception e) {
            hiba("Hiba", e.getMessage());
        }
    }

    public void kategoriaTorlesClick(ActionEvent actionEvent) {
        int kivalasztott = kategoriaListView.getSelectionModel().getSelectedIndex();
        if (kivalasztott == -1) {
            figyelmeztetes("Nincs kiv??lasztva elem", "A t??rl??shez el??bb v??lasszon ki egy elemet");
            return;
        }

        FogasKategoria kategoria = kategoriaListView.getSelectionModel().getSelectedItem();

        if (!elfogadas("Biztos szeretn??d t??r??lni a(z) " + kategoria.getNev() + " kateg??ri??t?")) {
            return;
        }

        try {
            if (menuDB.kategoriaTorles(kategoria)) {
                informacio("Sikeres t??rl??s");
            } else {
                figyelmeztetes("Sikertelen t??rl??s");
            }
        } catch (SQLException e) {
            hiba("Sikertelen t??rl??s", e.getMessage());
        }
        adatBetoltes();
    }
}