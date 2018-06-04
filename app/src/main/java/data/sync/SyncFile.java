package data.sync;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.annotation.Nullable;

import data.drive.Contract;

/**
 * Created by Ahmad on 03/20/18.
 * All rights reserved.
 */

public class SyncFile {
    private SyncFile parent;
    private String name;

    public SyncFile(String name) {
        this.name = name;
    }

    public SyncFile(SyncFile parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Nullable
    public SyncFile getParent() {
        return parent;
    }

    public ArrayList<SyncFile> getParentHierarchy() {
        LinkedList<SyncFile> linkedList = new LinkedList<>();
        SyncFile parent = this.parent;
        while (parent != null) {
            linkedList.addFirst(parent);
            parent = parent.getParent();
        }
        return new ArrayList<>(linkedList);
    }

    public String getParentHierarchyString() {
        StringBuilder hierarchyString = new StringBuilder();
        ArrayList<SyncFile> hierarchy = getParentHierarchy();
        for (SyncFile parent : hierarchy) {
            hierarchyString.append(Contract.HIERARCHY_SEPARATOR).append(parent.getName());
        }
        return hierarchyString.toString();
    }

    public ArrayList<SyncFile> getHierarchy() {
        LinkedList<SyncFile> linkedList = new LinkedList<>();
        SyncFile parent = this;
        while (parent != null) {
            linkedList.addFirst(parent);
            parent = parent.getParent();
        }
        return new ArrayList<>(linkedList);
    }

    public String getHierarchyString() {
        StringBuilder hierarchyString = new StringBuilder();
        ArrayList<SyncFile> hierarchy = getHierarchy();
        for (SyncFile file : hierarchy) {
            hierarchyString.append(Contract.HIERARCHY_SEPARATOR).append(file.getName());
        }
        return hierarchyString.toString();
    }

    public String getName() {
        return name;
    }

    public SyncFile getChild(String childName) {
        return new SyncFile(this.clone(), childName);
    }

    public SyncFile clone() {
        SyncFile clone;
        if (parent == null) {
            clone = new SyncFile(name);
        } else {
            clone = new SyncFile(parent.clone(), name);
        }
        return clone;
    }
}
