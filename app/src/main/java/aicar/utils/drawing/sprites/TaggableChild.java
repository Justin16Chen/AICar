package aicar.utils.drawing.sprites;

import java.util.ArrayList;

public class TaggableChild<E extends TaggableChild<E>> {
    private ArrayList<E> children;
    private ArrayList<String> tags;
    
    public TaggableChild() {
        children = new ArrayList<>();
        tags = new ArrayList<>();
    }
    public TaggableChild(ArrayList<String> tags) {
        children = new ArrayList<>();
        this.tags = tags;
    }

    // returns only direct children - does not include self
    public ArrayList<E> getDirectChildren() {
        return children;
    }
    // returns only direct children that have a matching tag - does not include self
    public ArrayList<E> getDirectChildren(String tag) {
        ArrayList<E> selectedChildren = new ArrayList<>();
        for (E child : children)
            if (child.hasTag(tag))
                selectedChildren.add(child);
        return selectedChildren;
    }

    // returns all children - and children of children - in a single list - includes self
    @SuppressWarnings("unchecked")
    public ArrayList<E> getAllChildren() {
        ArrayList<E> allChildren = new ArrayList<>();
        try {
            allChildren.add((E) this); // Explicit cast, assumed safe
        } catch (ClassCastException e) {
            throw new IllegalStateException("Invalid type conversion in getAllChildren: " + this.getClass().getName(), e);
        }
        for (E child : children) 
            allChildren.addAll(child.getAllChildren()); 
        return allChildren;
    }
    // returns all children that have a matching tag - and children of children - in a single list 
    public ArrayList<E> getAllChildren(String tag) {
        ArrayList<E> selectedChildren = new ArrayList<>();
        for (E  child : getAllChildren())
            if (child.hasTag(tag))
                selectedChildren.add(child);
        return selectedChildren;
    }
    public void addChild(E child) {
        children.add(child);
    }
    public void removeChild(E child) {
        children.remove(child);
    }
    public void addTag(String tag) {
        tags.add(tag);
    }
    public void clearTags() {
        tags.clear();
    }
    public boolean hasTag(String tag) {
        return tags.indexOf(tag) != -1;
    }
}