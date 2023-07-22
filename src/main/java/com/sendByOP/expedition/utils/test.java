package com.sendByOP.expedition.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class test {

    public static class Coord {
        private float x;
        private float y;

        public Coord(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static boolean doublon(float[] t) {
        int cpt = 0;
        for (int i = 0; i < t.length-2; i++) {
            for (int j = i+1; j < t.length - 1; j++) {
                if (t[i] == t[j]) {
                    cpt ++;
                }
            }
        }
        if (cpt == 0)
            return true;
        else
            return false;
    }

    public static boolean doublon2(float[] t) {
        int cpt = 0;
        for (int i = 0; i < t.length-2; i++) {
            for (int j = i+1; j < t.length - 1; j++) {
                if (t[i] == t[j]) {
                    return false;
                }
            }
        }
        return true;

    }

    public static boolean doublon3(float[] t) {
        Arrays.sort(t);

        for (int i = 1; i < t.length; i++) {
            if (t[i] == t[i-1]) {
                return false;
            }
        }
        return true;
    }

    public static boolean doublon4(float[] t) {

        Set<Float> en = new HashSet<>();
        for (int i = 0; i < t.length; i++) {
            en.add(t[i]);
        }
        if (en.size() == t.length) {
            return true;
        } else {
            return false;
        }
    }


    public static double arrosage(Coord[] t, Coord[] tab) {
        double k = 0;
        for (int i = 0; i < t.length ; i++) {
            for (int j = 0; j < tab.length; j++) {
                k =  (k + Math.sqrt((t[i].x-tab[j].x)*(t[i].x-tab[j].x) + (t[i].y -tab[j].y)*(t[i].y -tab[j].y)));
            }
        }
        System.out.println(k);
        System.out.println(t.length+" "+tab.length);
        return k/(tab.length);
    }

    public static double arrosage2(Coord[] t, Coord[] tab) {
        int i;
        float s1, s2, s3, s4, m1, m2, s;
        s1=s2=s3=s4=m1=m2=s=0;

        for (int j = 0; j < tab.length; j++) {
            s1 = s1 + tab[j].y*tab[j].y;
            s2 = s2 + tab[j].x*tab[j].x;
            s3 = s3 + tab[j].y;
            s4 = s4 + tab[j].x;
        }

        for (int j = 0; j < t.length; j++) {
            m1 = m1 + t[j].y*t[j].y;
            m2 = m2 + t[j].x*t[j].x;
            s = s + s1 +s2+ m1+ m2 -2*t[j].y*s3 -2*t[j].x*s4;
        }
        return s;
    }


    public static void main(String[] args) {
        Random random = new Random();
        /*float[] t = new float[100000];
        for (int i = 0; i < t.length; i++) {
            t[i] = random.nextFloat();
        }

        ///exercie 1
        long tempsDebut1 = System.currentTimeMillis();
        System.out.println(doublon(t));
        long tempsFin1 = System.currentTimeMillis();
        float seconds1 = (tempsFin1 - tempsDebut1) / 1000F;
        System.out.println("t1 Opération effectuée en: "+ Float.toString(seconds1) + " secondes.");

        ////
        long tempsDebut = System.currentTimeMillis();
        System.out.println(doublon(t));
        long tempsFin = System.currentTimeMillis();
        float seconds = (tempsFin - tempsDebut) / 1000F;
        System.out.println("t2 Opération effectuée en: "+ Float.toString(seconds) + " secondes.");

        ///
        long tempsDebut2 = System.currentTimeMillis();
        System.out.println(doublon3(t));
        long tempsFin2 = System.currentTimeMillis();
        float seconds2 = (tempsFin2 - tempsDebut2) / 1000F;
        System.out.println("t3 Opération effectuée en: "+ Float.toString(seconds2) + " secondes.");

        //////////
        long tempsDebut3 = System.currentTimeMillis();
        System.out.println(doublon4(t));
        long tempsFin3 = System.currentTimeMillis();
        float seconds3 = (tempsFin3 - tempsDebut3) / 1000F;
        System.out.println("t4 Opération effectuée en: "+ Float.toString(seconds3) + " secondes.");*/

        //Exercice 2
        int n = 60000;
        int m = 50000;
        Coord[] arbres = new Coord[n];
        Coord[] points = new Coord[m];
        for (int i = 0; i < n; i++) {
            arbres[i] = new Coord(random.nextFloat(),random.nextFloat());
        }
        for (int i = 0; i < m; i++) {
            points[i] = new Coord(random.nextFloat(),random.nextFloat());
        }


        long tempsDebut4 = System.currentTimeMillis();
        System.out.println("le nombre est "+ arrosage(arbres, points));
        long tempsFin4 = System.currentTimeMillis();
        float seconds4 = (tempsFin4 - tempsDebut4) / 1000F;
        System.out.println("t4 Opération effectuée en: "+ Float.toString(seconds4) + " secondes.");

        long tempsDebut5 = System.currentTimeMillis();
        System.out.println("le nombre est "+ arrosage2(arbres, points));
        long tempsFin5 = System.currentTimeMillis();
        float seconds5 = (tempsFin5 - tempsDebut5) / 1000F;
        System.out.println("t5 Opération effectuée en: "+ Float.toString(seconds5) + " secondes.");
    }

}
