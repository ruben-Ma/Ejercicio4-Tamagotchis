package org.cuatrovientos.dam.psp.tamagotchis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa un Tamagotchi.
 * Cada instancia está diseñada para correr en su propio hilo.
 */
public class Tamagotchi implements Runnable {

    private final String name;

    
    private volatile boolean alive;
    
    
     //atriibutos de cocurrencia
      private final AtomicInteger dirtiness; // la sucio de de tamagotchi 0-10
     // 'BlockingQueue' es el "buzón" thread-safe para recibir órdenes.
      private final BlockingQueue<Action> actionQueue;

      // atributos de vida
      private long startTime;      // Cuándo nació (para la muerte por edad)
      private long lastDirtTick;   // Cuándo se ensució por última vez
    
    
      public Tamagotchi(String name) {
	    this.name = name;
	    this.alive = true;
	    
	    // nuevos atribustos del tamagotchi
	    this.dirtiness = new AtomicInteger(0);
	    this.actionQueue = new LinkedBlockingQueue<>(); // Una cola enlazada, sin límite
	}

    
      
    private void handleAction(Action action) throws InterruptedException{
    	
    	System.out.println(" [" + name + "] (Accion) Recibida orden"+ action);
    	
    	
    	switch(action) {
    	
    	case FEED:
        	System.out.println(" [" + name + "] (Accion) Empieza a comer");
        	Thread.sleep(3000);
        	System.out.println(" [" + name + "] (Accion) Acaba de comer");
        	
        	break;
        	
    	case CLEAN:
        	System.out.println(" [" + name + "] (Accion) Empieza a bañarse");
        	Thread.sleep(5000);
        	this.dirtiness.set(0);
        	System.out.println(" [" + name + "] (Accion) Esta limpio");
        	break;
        	
    	case PLAY:
        	System.out.println(" [" + name + "] (Accion) !A jugarrrr!!!");
        	
        	break;
        }
    	
    }
      
   private void checkAutonomousStatus() {
	   
	   long now = System.currentTimeMillis();
	   
	   if( now - startTime > 300_000) {
       	System.out.println(" [" + name + "] (Vida) Mi tiempo ha pasado... voy a morir");
       	this.alive = false;
       	return;

	   }
	   
	   if( now - lastDirtTick > 20_000) {
		   this.lastDirtTick = now; 
		 int currentDirt = dirtiness.incrementAndGet();
		 
     	System.out.println(" [" + name + "] (Vida) Mi suciedad esta en "+ currentDirt);
               
     	        if(currentDirt == 5) {
     	        	System.out.println(" [" + name + "] (Vida) Ya empiezo a estar muy sucio... voy por la mitad antes de morir de guarro");
     	        }
     	        
     	        if(currentDirt >= 10) {
     	        	System.out.println(" [" + name + "] (Vida) Me muero por que no me has lavado.... Malditooo");
     	        	this.alive = false;
     	        	return; 

     	        }
	   }
	   
	   
	   
   }
    
    @Override
    public void run() {
        Thread.currentThread().setName("Tamagotchi-" + name);
        
        System.out.println(" ¡" + name + " ha nacido!");
        
        
        this.startTime = System.currentTimeMillis();
        this.lastDirtTick = System.currentTimeMillis();

        try {
        
            while (this.alive) {
                
           
               Action nextAction = actionQueue.poll(1, TimeUnit.SECONDS);
               
               if (nextAction != null) {
            	   handleAction(nextAction);
               }
               
               checkAutonomousStatus();
            }
        } catch (InterruptedException e) {
            this.alive = false;
            System.out.println(" [" + name + "] ha sido interrumpido.");
        }

        System.out.println(" --- " + name + " HA MUERTO. --- ");
    }
   
    
    
    

    public boolean isAlive() {
        return this.alive;
    }
    
    public String getName() {
        return this.name;
    }
    
    
    //para que el cuidador mande ordenes al buzon actionQueue
    
    public void feed() {
    	this.actionQueue.offer(Action.FEED);
    }
    
    public void clean() {
    	this.actionQueue.offer(Action.CLEAN);
    }
    
    public void play() {
    	this.actionQueue.offer(Action.PLAY);
    }
    
    public String getStatus() {
    	return String.format(" ->> | VIVo : %-5b | Suciedad: %d/10", name, alive, dirtiness.get());
    }
}
