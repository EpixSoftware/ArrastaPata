package br.com.epix.arrastapata;

/**
 * Created by martelli on 5/29/16.
 */
public class Convite {

    private String numero;
    private int sorteado;

    public Convite(String numero, int sorteado) {
        this.numero = numero;
        this.sorteado = sorteado;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getSorteado() {
        return sorteado;
    }

    public void setSorteado(int sorteado) {
        this.sorteado = sorteado;
    }

    @Override
    public String toString() {
        return numero;
    }
}
