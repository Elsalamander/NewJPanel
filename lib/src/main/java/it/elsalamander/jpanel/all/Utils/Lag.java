package it.elsalamander.jpanel.all.Utils;

/*********************************************************************
 * Gestion del contatore dei tick del server
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v2.0.1
 * 
 *********************************************************************/
public class Lag implements Runnable {
	
	private static Lag lag;
	
    public int TICK_COUNT;
    public long[] TICKS;
    public long LAST_TICK;
    

    /**
     * Costruttore privato
     */
    private Lag(){
    	this.TICK_COUNT = 0;
    	this.TICKS = new long[600];
    	this.LAST_TICK = 0L;
    }
    
    /**
     * Instanza Lag
     * @return
     */
    public static Lag getInstance() {
    	if(lag == null) {
    		lag = new Lag();
    	}
        return lag;
    }

    /**
     * Ritorna i TPS
     * @return
     */
    public double getTPS() {
        return this.getTPS(100);
    }

    /**
     * Ritorna i TPS degli ultimi tick richiesti
     * @return
     */
    public double getTPS(int ticks) {
        if (TICK_COUNT < ticks) {
            return 20.0D;
        }
        int target = (TICK_COUNT - 1 - ticks) % TICKS.length;

        boolean inBounds = (target >= 0) && (target < TICKS.length);
        if (inBounds) {
            long elapsed = System.currentTimeMillis() - TICKS[target];
            return ticks / (elapsed / 1000.0D);
        } else {
            return 20.0D;
        }
    }

    /**
     * Tempo passato al tick richiesto
     * @param tickID
     * @return
     */
    public long getElapsed(int tickID) {
        if (TICK_COUNT - tickID >= TICKS.length) {
        }

        long time = TICKS[(tickID % TICKS.length)];
        return System.currentTimeMillis() - time;
    }

    public void run() {
        TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();

        TICK_COUNT += 1;
    }
}
