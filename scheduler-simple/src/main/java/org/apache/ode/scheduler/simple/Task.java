package org.apache.ode.scheduler.simple;

/**
 * The thing that we schedule.
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 *
 */
class Task {
	/** Scheduled date/time. */
    public long schedDate;

    Task(long schedDate) {
        this.schedDate = schedDate;
    }
}
