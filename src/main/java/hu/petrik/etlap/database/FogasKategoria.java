package hu.petrik.etlap.database;

public class FogasKategoria {

    private int id;
    private String nev;

    public FogasKategoria(int id, String nev) {
        this.id = id;
        this.nev = nev;
    }

    public FogasKategoria(String nev) {
        this.nev = nev;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public int getId() {
        return id;
    }

    public String getNev() {
        return nev;
    }

    @Override
    public String toString() {
        return getNev();
    }
}
