package org.cuatrovientos.dam.psp.tamagotchis;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Clase principal que actúa como el Cuidador.
 * Gestiona el ciclo de vida de los Tamagotchis.
 */
public class Cuidador {

    private static final int NUM_TAMAGOTCHIS = 3;

    private final ExecutorService mundo;
    
    
    private final Map<String, Tamagotchi> misTamagotchis;

    public Cuidador() {
        this.mundo = Executors.newFixedThreadPool(NUM_TAMAGOTCHIS);
        this.misTamagotchis = new HashMap<>();
    }

    
    public void iniciar() {
        System.out.println("--- Iniciando el Mundo Tamagotchi ---");
        
        for (int i = 0; i < NUM_TAMAGOTCHIS; i++) {
            String name = "Tama-" + i;
            Tamagotchi t = new Tamagotchi(name);
            misTamagotchis.put(name, t);

            
            mundo.submit(t);
        }
        
        System.out.println("--- " + NUM_TAMAGOTCHIS + " Tamagotchis han sido lanzados. ---");

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nPresiona ENTER para ver el estado o escribe 'salir' para terminar.");
        
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("salir")) {
                break;
            }
            
            System.out.println("\n[Cuidador] Estado actual:");
            for (Tamagotchi t : misTamagotchis.values()) {
                System.out.println("  -> " + t.getName() + " | Vivo: " + t.isAlive());
            }
        }
        
        apagarMundo();
        scanner.close();
    }

    
    private void apagarMundo() {
        System.out.println("--- APAGANDO EL MUNDO ---");
        
        mundo.shutdownNow();
        try {
           
            mundo.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Problemas al apagar el mundo.");
        }
        System.out.println("--- El mundo está en silencio. ---");
    }

    
    public static void main(String[] args) {
        Cuidador cuidador = new Cuidador();
        cuidador.iniciar();
    }
}
