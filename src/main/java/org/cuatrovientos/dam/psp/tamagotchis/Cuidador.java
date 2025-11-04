package org.cuatrovientos.dam.psp.tamagotchis;

import java.util.HashMap;
import java.util.InputMismatchException; 
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * esta es la clase principal, el cuidador.
 * maneja a todos los tamagotchis (los hilos).
 */
public class Cuidador {
    
    // el scanner para leer del teclado
    private final Scanner scanner;

    // cuantos tamas vamos a tener
    private static final int NUM_TAMAGOTCHIS = 3;

    // el undo que controla los hilos
    private final ExecutorService mundo;
    
    
    // un mapa para guardar los tamas y buscarlos por nombre
    private final Map<String, Tamagotchi> misTamagotchis;

    public Cuidador() {
        // creamos el 'mundo' con 3 hilos, uno pa cada tama
        this.mundo = Executors.newFixedThreadPool(NUM_TAMAGOTCHIS);
        // creamos el mapa vacio
        this.misTamagotchis = new HashMap<>();
        // creamos el unico scanner que usaremos
        this.scanner = new Scanner(System.in);
    }

    
    public void iniciar() {
        System.out.println("--- Iniciando el Mundo Tamagotchi ---");
        
        // bucle para crear los 3 tamas
        for (int i = 0; i < NUM_TAMAGOTCHIS; i++) {
           
            // les damos un nombre, Tama-0, Tama-1
            String name = "Tama-" + i; 
            // velocidades de comida 
            int speed = (i + 2) * 1000;
            
            // creamos el tama y le pasamos el scanner compartido 
            Tamagotchi t = new Tamagotchi(name, speed, this.scanner);
            
            // lo metemos al mapa
            misTamagotchis.put(name, t);
            // lo lanzamos al mundo! esto llama a su run
            mundo.submit(t);
        }
        
        System.out.println("--- " + NUM_TAMAGOTCHIS + " Tamagotchis han sido lanzados. ---");
        
        
        buclePrincipalDelCuidador(); // ahora llamamos al menu
        
        
        apagarMundo(); // cuando salgamos del menu, apagamos todo
        scanner.close(); // cerramos el scanner al final
    }
    
    
    private void buclePrincipalDelCuidador() {
        boolean salir = false; // variable para controlar el bucle del menu
        while (!salir) {
            
            // bloqueamos el scanner para que no se pise con el juego
            synchronized (this.scanner) {
                printMenu(); // imprime las opciones
                try {
                    int opcion = scanner.nextInt(); // lee el numero
                    scanner.nextLine(); // limpia el buffer 

                    switch (opcion) { // un switch para ver que hacemos
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
                            intentarMatar(); // la opcion de matar
                            break;
                            
                        case 0:
                            salir = true; // cambiamos  a true para salir del while
                            break;
                        default:
                            System.out.println("Opcion no valida.");
                    }
                } catch (InputMismatchException e) { // por si escriben letras en vez de numeros
                    System.out.println("Por favor, introduce un numero.");
                    scanner.nextLine(); // limpiamos el buffer si puso letras
                }
            } // aqui se suelta el bloqueo  del scanner
            
            
            // pequeña pausa para que otros hilos respiren
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }
    
    
    // metodo solo para imprimir el texto del menu
    private void printMenu() {
        System.out.println("\n------------ MENU DEL CUIDADOR ---------------");
        System.out.println("1.        Alimentar un Tamagotchi");
        System.out.println("2.        Limpiar un Tamagotchi");
        System.out.println("3.        Jugar con un Tamagotchi");
        System.out.println("4.        Ver estado de todos");
        System.out.println("5.        Intentar matar (solo si esta ocioso)");
        System.out.println("0.        Salir");
        System.out.println("Elige una opcion: ");
    }

    
    private void interactuar(Action accion) {
        Tamagotchi t = seleccionarTamagotchi(); // primero elegimos a quien
        if (t != null) { // si el tama existe y esta vivo
            System.out.println(" Enviando orden '" + accion + "' a " + t.getName());
            switch (accion) { // segun la accion, llamamos al metodo
                case FEED: t.feed(); break; //  solo mete la orden en el buzon
                case CLEAN: t.clean(); break;
                case PLAY: t.play(); break;
            }
        }
    }

    
    private void mostrarEstado() {
        System.out.println("\n Obteniendo estado de todos:");
        for (Tamagotchi t : misTamagotchis.values()) { // recorre el mapa de tamas
            System.out.println(t.getStatus()); // llama al get) de cada uno
        }
    }
    
    
    private Tamagotchi seleccionarTamagotchi() {
        System.out.println(" ¿Con que Tamagotchi? (Vivos)");
        
        for (Tamagotchi t : misTamagotchis.values()) { // recorre el mapa
            if (t.isAlive()) { // y muestra solo los vivos
                System.out.println("  - " + t.getName());
            }
        }
        System.out.print("Escribe el nombre (ej. Tama-0): ");
        String nombre = scanner.nextLine(); // pide el nombre por teclado
        
        Tamagotchi t = misTamagotchis.get(nombre); // lo busca en el mapa
        if (t == null || !t.isAlive()) { // comprueba que existe y esta vivo
            System.out.println("Ese Tamagotchi no existe o esta muerto.");
            return null;
        }
        return t; // devuelve el tama que hemos encontrado
    }

    
    private void intentarMatar() {
        Tamagotchi t = seleccionarTamagotchi(); // elige a quien matar
        if (t != null) { // si existe...
            System.out.println(" Vas a matar a " + t.getName() + "...");
            boolean exito = t.requestKill(); // llamamos al metodo del tamagotchi
            
            if (exito) { // el tama devuelve true si estaba ocioso
                System.out.println("MUERTOOOO.");
            } else { // devuelve false si estaba ocupado
                System.out.println("No puedes.");
            }
        }
    }
    
    private void apagarMundo() {
        System.out.println("--- APAGANDO EL MUNDO ---");
        
        // manda una interrupcion  a todos los hilos
        mundo.shutdownNow(); 
        try {
            
            // espera 5 segundos a que los hilos terminen
            mundo.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Problemas al apagar el mundo.");
        }
        System.out.println("--- TODOS MUERTOS ---");
    }

    
    public static void main(String[] args) {
        Cuidador cuidador = new Cuidador(); // crea el objeto cuidador
        cuidador.iniciar(); // arranca el programa
    }
}