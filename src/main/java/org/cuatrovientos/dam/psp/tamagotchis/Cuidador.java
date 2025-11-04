package org.cuatrovientos.dam.psp.tamagotchis;

import java.util.HashMap;
import java.util.InputMismatchException; 
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
    
    private final Scanner scanner;

    private static final int NUM_TAMAGOTCHIS = 3;

    private final ExecutorService mundo;
    
    
    private final Map<String, Tamagotchi> misTamagotchis;

    public Cuidador() {
        this.mundo = Executors.newFixedThreadPool(NUM_TAMAGOTCHIS);
        this.misTamagotchis = new HashMap<>();
        this.scanner = new Scanner(System.in);
    }

    
    public void iniciar() {
        System.out.println("--- Iniciando el Mundo Tamagotchi ---");
        
        for (int i = 0; i < NUM_TAMAGOTCHIS; i++) {
           
            String name = "Tama-" + i; 
            int speed = (i + 2) * 1000;
            
            Tamagotchi t = new Tamagotchi(name, speed, this.scanner);
            
            misTamagotchis.put(name, t);
            mundo.submit(t);
        }
        
        System.out.println("--- " + NUM_TAMAGOTCHIS + " Tamagotchis han sido lanzados. ---");
        
        
        buclePrincipalDelCuidador();
        
        
        apagarMundo();
        scanner.close();
    }
    
    
    private void buclePrincipalDelCuidador() {
        boolean salir = false;
        while (!salir) {
            
            synchronized (this.scanner) {
                printMenu(); 
                try {
                    int opcion = scanner.nextInt();
                    scanner.nextLine(); 

                    switch (opcion) {
                        case 1:
                            interactuar(Action.FEED);
                            break;
                        case 2:
                            interactuar(Action.CLEAN);
                            break;
                        case 3:
                            interactuar(Action.PLAY);
                            break;
                        case 4:
                            mostrarEstado();
                            break;
                            
                       
                        case 5:
                            intentarMatar();
                            break;
                            
                        case 0:
                            salir = true;
                            break;
                        default:
                            System.out.println("Opción no válida.");
                    }
                } catch (InputMismatchException e) { 
                    System.out.println("Por favor, introduce un número.");
                    scanner.nextLine(); // Limpiar el buffer
                }
            } 
            
            
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }
    
    
    private void printMenu() {
        System.out.println("\n--- MENÚ DEL CUIDADOR ---");
        System.out.println("1. Alimentar un Tamagotchi");
        System.out.println("2. Limpiar un Tamagotchi");
        System.out.println("3. Jugar con un Tamagotchi");
        System.out.println("4. Ver estado de todos");
        System.out.println("5. Intentar matar (solo si está ocioso)"); // <-- NUEVA LÍNEA
        System.out.println("0. Salir");
        System.out.print("Elige una opción: ");
    }

    
    private void interactuar(Action accion) {
        Tamagotchi t = seleccionarTamagotchi();
        if (t != null) {
            System.out.println("[Cuidador] Enviando orden '" + accion + "' a " + t.getName());
            switch (accion) {
                case FEED: t.feed(); break;
                case CLEAN: t.clean(); break;
                case PLAY: t.play(); break;
            }
        }
    }

    
    private void mostrarEstado() {
        System.out.println("\n[Cuidador] Obteniendo estado de todos:");
        for (Tamagotchi t : misTamagotchis.values()) {
            System.out.println(t.getStatus());
        }
    }
    
    
    private Tamagotchi seleccionarTamagotchi() {
        System.out.println("[Cuidador] ¿Con qué Tamagotchi? (Vivos)");
        
        for (Tamagotchi t : misTamagotchis.values()) {
            if (t.isAlive()) {
                System.out.println("  - " + t.getName());
            }
        }
        System.out.print("Escribe el nombre (ej. Tama-0): ");
        String nombre = scanner.nextLine();
        
        Tamagotchi t = misTamagotchis.get(nombre);
        if (t == null || !t.isAlive()) {
            System.out.println("Ese Tamagotchi no existe o está muerto.");
            return null;
        }
        return t;
    }

    
    private void intentarMatar() {
        Tamagotchi t = seleccionarTamagotchi();
        if (t != null) {
            System.out.println("[Cuidador] Solicitando muerte a " + t.getName() + "...");
            boolean exito = t.requestKill();
            
            if (exito) {
                System.out.println("[Cuidador] ...la solicitud fue aceptada.");
            } else {
                System.out.println("[Cuidador] ...la solicitud fue rechazada (está ocupado).");
            }
        }
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