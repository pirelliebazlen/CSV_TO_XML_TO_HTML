package Etape1_Conversion;

import java.io.Console;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("1.Main");
            ConvertToXML.convertir();
            System.out.println("fait");
        } catch (IOException e) {
            System.err.println("Erreur durant la conversion CSV vers XML.");
            e.printStackTrace();
        }
    }
}