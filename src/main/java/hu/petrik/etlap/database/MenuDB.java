package hu.petrik.etlap.database;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDB {

    private final Connection conn;

    public static String DB_DRIVER = "mysql";
    public static String DB_HOST = "localhost";
    public static String DB_PORT = "3306";
    public static String DB_USERNAME = "root";
    public static String DB_PASSWORD = "";
    public static String DB_NAME = "etlapdb";


    public MenuDB() throws SQLException {
        String url = String.format("jdbc:%s://%s:%s/%s", DB_DRIVER, DB_HOST, DB_PORT, DB_NAME);
        conn = DriverManager.getConnection(url, DB_USERNAME, DB_PASSWORD);
    }

    public List<Fogas> getMenu() throws SQLException {
        List<Fogas> menu = new ArrayList<>();

        String sql = "SELECT etlap.id, etlap.nev, etlap.leiras, etlap.ar, kategoria.nev AS kategoria " +
                "FROM `etlap` INNER JOIN kategoria ON etlap.kategoria_id=kategoria.id;";

        Statement statement = conn.createStatement();
        ResultSet eredmeny = statement.executeQuery(sql);

        while (eredmeny.next()) {
            menu.add(getEgyFogas(eredmeny));
        }
        return menu;

    }

    public List<Fogas> getSzurtMenu(List<FogasKategoria> kategoriak) throws SQLException {
        if (kategoriak.size() == 0) {
            return getMenu();
        }
        String sql = "SELECT etlap.id, etlap.nev, etlap.leiras, etlap.ar, kategoria.nev AS kategoria " +
                "FROM etlap INNER JOIN kategoria ON etlap.kategoria_id = kategoria.id AND etlap.kategoria_id = ?;";
        PreparedStatement statement = conn.prepareStatement(sql);
        List<Fogas> menu = new ArrayList<>();
        for (FogasKategoria category : kategoriak) {
            statement.setInt(1, category.getId());
            ResultSet eredmenyek = statement.executeQuery();
            while (eredmenyek.next()) {
                menu.add(getEgyFogas(eredmenyek));
            }
        }
        return menu;

    }

    public Fogas getFogasById(int id) throws SQLException {
        String sql = "SELECT * FROM etlap WHERE id = ?";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return getEgyFogas(resultSet);
    }

    private Fogas getEgyFogas(ResultSet eredmeny) throws SQLException {
        int id = eredmeny.getInt("id");
        String nev = eredmeny.getString("nev");
        String leiras = eredmeny.getString("leiras");
        int ar = eredmeny.getInt("ar");
        String kategoria = eredmeny.getString("kategoria");
        return new Fogas(id, nev, new FogasKategoria(kategoria), leiras, ar);
    }

    public List<FogasKategoria> getKategoriak() throws SQLException {
        String sql = "SELECT id, nev FROM kategoria";

        Statement statement = conn.createStatement();
        ResultSet eredmenyek = statement.executeQuery(sql);

        List<FogasKategoria> kategoriak = new ArrayList<>();
        while (eredmenyek.next()) {
            kategoriak.add(new FogasKategoria(
                    eredmenyek.getInt("id"),
                    eredmenyek.getString("nev")
            ));
        }
        return kategoriak;
    }

    public boolean FogasHozzaad(Fogas fogas) throws SQLException {
        String sql = "INSERT INTO etlap (nev, leiras, ar, kategoria_id) VALUES (?, ?, ?, ?)";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, fogas.getNev());
        statement.setString(2, fogas.getLeiras());
        statement.setInt(3, fogas.getAr());
        statement.setInt(4, fogas.getKategoria().getId());
        return statement.executeUpdate() >= 1;
    }

    public boolean FogasTorles(int id) throws SQLException {
        String sql = "DELETE FROM etlap WHERE id = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, id);
        return statement.executeUpdate() >= 1;
    }

    public boolean arNovekedesPenzben(int id, int asd) throws SQLException {
        String sql = "UPDATE etlap SET ar = ar + ?";
        PreparedStatement statement = null;
        if (id == -1) {
            statement = conn.prepareStatement(sql);
            statement.setInt(1, asd);
        } else {
            sql += " WHERE id = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, asd);
            statement.setInt(2, id);
        }
        return statement.executeUpdate() >= 1;
    }

    public boolean arNovekedesSzazalekban(int id, int szazalek) throws SQLException {
        double percentage = (double) szazalek / 100 + 1;
        String sql = "UPDATE etlap SET ar = ar * ?";
        PreparedStatement statement = null;
        if (id == -1) {
            statement = conn.prepareStatement(sql);
            statement.setDouble(1, percentage);
        } else {
            sql += " WHERE id = ?";
            statement = conn.prepareStatement(sql);
            statement.setDouble(1, percentage);
            statement.setInt(2, id);
        }
        return statement.executeUpdate() >= 1;
    }

    public boolean kategoriaTorles(FogasKategoria kategoria) throws SQLException {
        kategoriaModositas(kategoria, new FogasKategoria(0, ""));
        String sql = "DELETE FROM kategoria WHERE id = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, kategoria.getId());
        return statement.executeUpdate() >= 1;
    }

    public boolean kategoriaModositas(FogasKategoria regiKategoria, FogasKategoria ujKategoria) throws SQLException {
        String sql = "UPDATE etlap SET kategoria_id = ? WHERE kategoria_id = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, ujKategoria.getId());
        statement.setInt(2, regiKategoria.getId());
        return statement.executeUpdate() >= 1;
    }

    public boolean kategoriaHozzaad(FogasKategoria kategoria) throws SQLException {
        String sql = "INSERT INTO kategoria (nev) VALUES (?)";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, kategoria.getNev());
        return statement.executeUpdate() >= 1;
    }

}
