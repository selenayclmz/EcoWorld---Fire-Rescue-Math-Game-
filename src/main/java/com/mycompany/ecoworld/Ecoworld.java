package com.mycompany.ecoworld;

import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.Random; 
import javax.swing.*;


public class Ecoworld extends JPanel implements ActionListener, MouseMotionListener, MouseListener {

    
    private Plane oyuncu; 
    
    private ArrayList<WaterOrb> suBalonlari = new ArrayList<>(); 
   
    private ArrayList<WaterDrop> atilanSular = new ArrayList<>(); 
   
    private ArrayList<GroundBlock> zeminBloklari = new ArrayList<>();
    
    
    private Timer oyunDongusu;


    private int eldekiSayi = 0; 
    private int toplamSkor = 0; 
    private final int HEDEF_SKOR = 250; 
  
    
    private int yikilanBinaSayisi = 0; 
    private final int MAX_YIKIM = 3; 
    
    private boolean oyunBitti = false; 
    private boolean kazandi = false; 
    
    
    private String feedbackMesaji = "";
    private int feedbackSayaci = 0; 

    
    private int yanginCikmaSayaci = 0;
    private int suCikmaSayaci = 0;

    
    public Ecoworld() {
        setFocusable(true); 
        addMouseMotionListener(this); 
        addMouseListener(this);       
        
        oyunuBaslat(); 
       
        oyunDongusu = new Timer(20, this); 
        oyunDongusu.start(); 
    }

  
    public void oyunuBaslat() {
        oyuncu = new Plane(400, 100); 
        toplamSkor = 0;
        eldekiSayi = 0;
        yikilanBinaSayisi = 0;
        oyunBitti = false;
        kazandi = false;

    
        suBalonlari.clear();
        atilanSular.clear();
        zeminBloklari.clear();

        
        for (int i = 0; i < 20; i++) {
            zeminBloklari.add(new GroundBlock(i * 40)); 
        }
    }

  
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2 = (Graphics2D) g; 
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        GradientPaint gp = new GradientPaint(0, 0, new Color(135, 206, 235), 0, 600, Color.WHITE);
        g2.setPaint(gp);
        g2.fillRect(0, 0, 800, 600); 

       
        for (GroundBlock b : zeminBloklari) b.draw(g2); 
        for (WaterOrb o : suBalonlari) o.draw(g2);      
        for (WaterDrop w : atilanSular) w.draw(g2);     


        if (eldekiSayi > 0) {
            g2.setColor(new Color(0, 100, 255, 60)); 
            g2.fillOval(oyuncu.x - 10, oyuncu.y - 10, 80, 50); 
        }
        oyuncu.draw(g2); 

       
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("SCORE: " + toplamSkor + " / " + HEDEF_SKOR, 20, 30);

        
        g2.setColor(new Color(192, 57, 43));
        g2.drawString("DESTROYED: " + yikilanBinaSayisi + " / " + MAX_YIKIM, 550, 30);

     
        if (eldekiSayi > 0) {
            g2.setColor(new Color(0, 51, 204));
            g2.drawString("LOADED: " + eldekiSayi, 20, 55); 
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("(Click to Drop)", 20, 70); 
        } else {
            g2.setColor(Color.RED);
            g2.drawString("CATCH DROPS!", 20, 55); 
        }

        
        if (feedbackSayaci > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            
            g2.setColor(feedbackMesaji.contains("LOST") || feedbackMesaji.contains("WRONG") ? Color.RED : new Color(39, 174, 96));
            g2.drawString(feedbackMesaji, 350, 200);
            feedbackSayaci--; 
        }

   
        if (oyunBitti) cizOyunSonu(g2);
    }

    
    private void cizOyunSonu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, 800, 600);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.PLAIN, 50));
        g2.drawString(kazandi ? "CITY SAVED!" : "CITY DESTROYED!", 230, 280);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Click to Restart", 320, 330);
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (oyunBitti) return; 
        
       
        if (toplamSkor >= HEDEF_SKOR) { oyunBitti = true; kazandi = true; }
        if (yikilanBinaSayisi >= MAX_YIKIM) { oyunBitti = true; kazandi = false; }
 
        
        yanginCikmaSayaci++;
       
        if (yanginCikmaSayaci > 230) { 
            int index = new Random().nextInt(zeminBloklari.size()); 
            GroundBlock b = zeminBloklari.get(index);
            
            if (!b.isBurnt()) { 
                if (!b.isOnFire()) {
                    b.ignite(); 
                } else {
                    b.growFire(); 
                    
                    if (b.isBurnt()) {
                        yikilanBinaSayisi++;
                        feedbackMesaji = "BUILDING LOST!";
                        feedbackSayaci = 40;
                    }
                }
            }
            yanginCikmaSayaci = 0;
        }

 
        suCikmaSayaci++;
      
        if (suCikmaSayaci > 50 && suBalonlari.size() < 5) { 
           
            ArrayList<Integer> ihtiyaclar = new ArrayList<>();
            for (GroundBlock b : zeminBloklari) {
                if (b.isOnFire() && !b.isBurnt()) ihtiyaclar.add(b.getAnswer());
            }

        
            if (!ihtiyaclar.isEmpty()) {
                int secilenCevap = ihtiyaclar.get(new Random().nextInt(ihtiyaclar.size()));
               
                suBalonlari.add(new WaterOrb(new Random().nextInt(750), -50, secilenCevap));
                suCikmaSayaci = 0;
            }
        }

        
        
       
        
      
        Iterator<WaterOrb> itOrb = suBalonlari.iterator();
        while (itOrb.hasNext()) {
            WaterOrb o = itOrb.next();
            o.move(); 
            
      
            if (o.getBounds().intersects(oyuncu.getBounds()) && eldekiSayi == 0) {
                eldekiSayi = o.getVal(); 
                itOrb.remove(); 
            } else if (o.y > 650) { 
                itOrb.remove(); 
            }
        }

        
        Iterator<WaterDrop> itDrop = atilanSular.iterator();
        while (itDrop.hasNext()) {
            WaterDrop w = itDrop.next();
            w.move();
            
            
            if (w.y > 480) { 
                
                for (GroundBlock b : zeminBloklari) {
                   
                    if (w.x > b.x && w.x < b.x + 40 && b.isOnFire() && !b.isBurnt()) {
                       
                        if (w.getVal() == b.getAnswer()) {
                            b.extinguish(); 
                            toplamSkor += 20;
                            feedbackMesaji = "SAVED!";
                        } else { 
                            b.growFire(); 
                            if(b.isBurnt()) { 
                                yikilanBinaSayisi++;
                                feedbackMesaji = "BUILDING LOST!";
                            } else {
                                feedbackMesaji = "WRONG!";
                            }
                        }
                        feedbackSayaci = 30;
                        break; 
                    }
                }
                itDrop.remove();
            }
        }
        repaint(); 
    }

    
    @Override
    public void mouseMoved(MouseEvent e) {
        if (!oyunBitti) {
            
            oyuncu.x = e.getX() - 30; 
            oyuncu.y = e.getY() - 15;
        }
    }
    
 
    @Override
    public void mousePressed(MouseEvent e) {
        if (oyunBitti) {
            oyunuBaslat(); 
        } else if (eldekiSayi > 0) {
           
            atilanSular.add(new WaterDrop(oyuncu.x + 30, oyuncu.y + 15, eldekiSayi));
            eldekiSayi = 0; 
        }
    }

 
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

   
    public static void main(String[] args) {
        JFrame f = new JFrame("EcoWorld – Fire Rescue Math Game"); 
        f.add(new Ecoworld()); 
        f.setSize(800, 600); 
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        f.setResizable(false); 
        f.setLocationRelativeTo(null); 
        f.setVisible(true); 
    }
}




class Plane {
    int x, y;

    Plane(int x, int y) { this.x = x; this.y = y; }
    
    Rectangle getBounds() { return new Rectangle(x, y, 60, 30); }
    
    void draw(Graphics2D g2) {
        
        g2.setColor(Color.WHITE); g2.fillOval(x, y, 60, 30); 
        g2.setColor(Color.RED); g2.fillOval(x + 22, y - 8, 15, 45); 
        g2.setColor(Color.CYAN); g2.fillOval(x + 45, y + 4, 10, 10); 
        g2.setColor(Color.GRAY); g2.fillOval(x - 5, y + 8, 8, 12); 
    }
}


class WaterOrb {
    int x, y, val;
    WaterOrb(int x, int y, int val) { this.x = x; this.y = y; this.val = val; }
    
    
    void move() { y += 2; } 
    
    Rectangle getBounds() { return new Rectangle(x, y, 30, 30); }
    int getVal() { return val; }
    
    void draw(Graphics2D g2) {
       
        g2.setColor(new Color(135, 206, 250, 150)); 
        g2.fillOval(x, y, 30, 30);
        g2.setColor(Color.BLUE); 
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, 30, 30);
        
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("" + val, x + 8, y + 22);
    }
}


class WaterDrop {
    int x, y, val;
    WaterDrop(int x, int y, int val) { this.x = x; this.y = y; this.val = val; }
    void move() { y += 10; } // Hızlı düşer (10 piksel)
    int getVal() { return val; }
    
    void draw(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 200)); // Koyu mavi
        g2.fillOval(x, y, 15, 20);
    }
}
 

class GroundBlock {
    int x, y = 480; 
    private int type; 
    private int fireLevel = 0; 
    private String question = ""; 
    private int answer = 0; 

    GroundBlock(int x) {
        this.x = x;
        this.type = new Random().nextInt(2); 
    }

    
    void ignite() {
        if (fireLevel == 0) {
            fireLevel = 1; 
            generateMath(); 
        }
    }
    
    
    void growFire() {
        if (fireLevel > 0 && fireLevel < 4) fireLevel++;
    }

    
    void extinguish() {
        fireLevel = 0;
    }
    
    
    boolean isOnFire() { return fireLevel > 0 && fireLevel < 4; }
    boolean isBurnt() { return fireLevel == 4; }
    int getAnswer() { return answer; }

    
    void generateMath() {
        Random r = new Random();
        int a = r.nextInt(9) + 1; 
        int b = r.nextInt(8) + 1;
      
        if (r.nextBoolean()) { question = a + "+" + b; answer = a + b; } 
        else { question = (a + b) + "-" + a; answer = b; }
    }

    
    void draw(Graphics2D g2) {
        
        if (fireLevel == 4) {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x + 5, y + 80, 30, 40); 
            return; 
        }

        
        if (type == 0) { 
            g2.setColor(new Color(149, 165, 166)); g2.fillRect(x + 2, y + 20, 36, 100); 
            g2.setColor(Color.YELLOW);
            
            for(int i = 0; i < 4; i++) { 
                g2.fillRect(x + 8, y + 30 + (i * 20), 8, 8); 
                g2.fillRect(x + 24, y + 30 + (i * 20), 8, 8); 
            }
        } else { 
            g2.setColor(new Color(121, 85, 72)); g2.fillRect(x + 15, y + 60, 10, 60); // Gövde
            g2.setColor(new Color(39, 174, 96)); g2.fillOval(x + 5, y + 20, 30, 50); // Yapraklar
        }

        
        if (isOnFire()) {
          
            int size = (fireLevel == 1) ? 20 : (fireLevel == 2) ? 35 : 55; 
            int offsetY = (fireLevel == 1) ? 0 : (fireLevel == 2) ? -10 : -25;
            
            
            Random r = new Random();
            int flicker = r.nextInt(5);
            

            g2.setColor(new Color(231, 76, 60, 200));
            int[] rx = {x, x + 20, x + 40}; 
            int[] ry = {y + 40, y + 20 - size - flicker + offsetY, y + 40};
            g2.fillPolygon(rx, ry, 3);
            
          
            g2.setColor(Color.ORANGE);
            int[] ox = {x + 10, x + 20, x + 30}; 
            int[] oy = {y + 40, y + 30 - size + offsetY, y + 40};
            g2.fillPolygon(ox, oy, 3);
            
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y - 10 + offsetY, 40, 20, 5, 5);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(question, x + 3, y + 4 + offsetY);
        }
    }
}