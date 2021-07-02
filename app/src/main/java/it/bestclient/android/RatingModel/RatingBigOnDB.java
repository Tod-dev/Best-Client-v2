package it.bestclient.android.RatingModel;

import java.util.Date;

public class RatingBigOnDB extends Rating{

    //attriubutes
    private String idEsercente;

    public RatingBigOnDB(String idEsercente,Date date, double voto,String numero, String commento) {
        super(numero,date, voto, commento);
        this.idEsercente = idEsercente;
    }

    public RatingBigOnDB(String idEsercente,String numero, Date date) {
        super(numero, date);
        this.idEsercente = idEsercente;
    }


    public String getIdEsercente() {
        return idEsercente;
    }

    public void setIdEsercente(String idEsercente) {
        this.idEsercente = idEsercente;
    }

    @Override
    public String toString() {
        return "RatingBigOnDB{" +
                super.toString() +
                "idEsercente='" + idEsercente + '\'' +
                '}';
    }
}
