package edu.uab.mukhtarlab.wkshelldecomposition.internal.model;

import edu.uab.mukhtarlab.wkshelldecomposition.internal.model.Shell;

import java.util.ArrayList;

public class Result {
    private ArrayList<Shell> shells = new ArrayList<Shell>();
    private int kMax = Integer.MIN_VALUE;
    private int kMin = Integer.MAX_VALUE;

    public ArrayList<Shell> getShells() {
        return shells;
    }

    public void setShells(ArrayList<Shell> shells) {
        this.shells = shells;
    }

    public void addShell(Shell shell) {
        int shellK = shell.getK();
        if(shellK>kMax) {
            kMax = shellK;
        }
        if (shellK<kMin) {
            kMin = shellK;
        }
        shells.add(shell);
    }

    public int getkMax() {
        return kMax;
    }

    public int getkMin() {
        return kMin;
    }

    public int getSize() {
        int size = 0;
        for(Shell shell : this.shells) {
            size += shell.getSize();
        }
        return size;
    }
}
