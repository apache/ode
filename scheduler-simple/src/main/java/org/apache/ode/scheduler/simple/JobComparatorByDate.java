package org.apache.ode.scheduler.simple;

import java.util.Comparator;

/**
 * Compare jobs, using scheduled date as sort criteria.
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
class JobComparatorByDate implements Comparator<Task> {

    public int compare(Task o1, Task o2) {
        long diff = o1.schedDate - o2.schedDate;
        if (diff < 0) return -1;
        if (diff > 0) return 1;
        return 0;
    }

}
