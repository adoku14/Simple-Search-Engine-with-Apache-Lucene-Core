/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

/**
 *
 * @author user
 */
public class Dokument {
    
    private int id;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    private int docid;
    private String title;
    private double score;
    private String path;

    public Dokument(int id, int docid, String title, double score, String path) {
        this.id = id;
        this.docid = docid;
        this.title = title;
        this.score = score;
        this.path = path;
    }

    public Dokument() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocid() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
    
    
}
