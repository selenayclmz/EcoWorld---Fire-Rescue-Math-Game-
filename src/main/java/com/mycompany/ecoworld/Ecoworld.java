package com.mycompany.ecoworld;

import java.awt.*; // Görsel çizim kütüphanesi (Renkler, Fontlar, Graphics)
import java.awt.event.*; // Olayları dinlemek için (Mouse, Klavye, Zamanlayıcı)
import java.util.ArrayList; // Dinamik listeler için (Ekle/Çıkar yapılabilir diziler)
import java.util.Iterator; // Listelerin içinde gezmek ve eleman silmek için
import java.util.Random; // Rastgele sayı üretmek için
import javax.swing.*; // Pencere ve panel araçları

// JPanel: Çizim yapacağımız "Tuval".
// ActionListener: Oyun döngüsü (Timer) her çalıştığında ne olacağını söyler.
// MouseMotionListener & MouseListener: Mouse hareketlerini ve tıklamalarını dinler.
public class Ecoworld extends JPanel implements ActionListener, MouseMotionListener, MouseListener {

    // --- OYUN NESNELERİ VE LİSTELER ---
    private Plane oyuncu; // Bizim uçağımız
    // Havada süzülen su toplarını tutan liste
    private ArrayList<WaterOrb> suBalonlari = new ArrayList<>(); 
    // Uçaktan aşağı attığımız suları tutan liste
    private ArrayList<WaterDrop> atilanSular = new ArrayList<>(); 
    // Yerdeki binaları ve ağaçları tutan liste
    private ArrayList<GroundBlock> zeminBloklari = new ArrayList<>();
    
    // Oyunun kalbi: Belirli aralıklarla (FPS) oyunu güncelleyen zamanlayıcı
    private Timer oyunDongusu;

    // --- OYUN DURUM DEĞİŞKENLERİ ---
    private int eldekiSayi = 0; // Uçak şu an hangi sayıyı taşıyor? (0 ise boş)
    private int toplamSkor = 0; // Oyuncunun puanı
    private final int HEDEF_SKOR = 250; // Kazanmak için gereken puan
    
    // Yıkım Kontrolü
    private int yikilanBinaSayisi = 0; // Kaç bina kül oldu?
    private final int MAX_YIKIM = 3; // 3 bina yanarsa oyun biter
    
    private boolean oyunBitti = false; // Oyun bitti mi?
    private boolean kazandi = false; // Kazandık mı kaybettik mi?
    
    // Ekranda çıkan "SAVED!" veya "WRONG!" yazıları için
    private String feedbackMesaji = "";
    private int feedbackSayaci = 0; // Yazının ekranda kalma süresi

    // Denge sayaçları (Yangın ve suyun ne sıklıkla çıkacağını belirler)
    private int yanginCikmaSayaci = 0;
    private int suCikmaSayaci = 0;

    // --- KURUCU METOD (Constructor) ---
    // Oyun ilk açıldığında burası çalışır.
    public Ecoworld() {
        setFocusable(true); // Klavye/Mouse odağını panele verir
        addMouseMotionListener(this); // Mouse hareketini takip et
        addMouseListener(this);       // Mouse tıklamasını takip et
        
        oyunuBaslat(); // Değişkenleri sıfırla ve nesneleri oluştur
        
        // 20 milisaniyede bir "actionPerformed" metodunu çağırır.
        // Bu oyunun hızını belirler. (Daha yüksek sayı = Daha yavaş oyun)
        oyunDongusu = new Timer(20, this); 
        oyunDongusu.start(); // Motoru çalıştır
    }

    // Oyunu sıfırlayan metod. 'R' tuşuna basınca veya ilk açılışta çağrılır.
    public void oyunuBaslat() {
        oyuncu = new Plane(400, 100); // Uçağı başlangıç konumuna koy
        toplamSkor = 0;
        eldekiSayi = 0;
        yikilanBinaSayisi = 0;
        oyunBitti = false;
        kazandi = false;

        // Eski listeleri temizle
        suBalonlari.clear();
        atilanSular.clear();
        zeminBloklari.clear();

        // 20 tane bina/ağaç oluştur ve yan yana diz (i * 40 piksel arayla)
        for (int i = 0; i < 20; i++) {
            zeminBloklari.add(new GroundBlock(i * 40)); 
        }
    }

    // --- ÇİZİM METODU (PaintComponent) ---
    // Ekrana her şeyi çizen ressam burasıdır. Sürekli tekrar çağrılır.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Ekranı temizle
        Graphics2D g2 = (Graphics2D) g; // Daha gelişmiş çizim aracı
        // Kenar yumuşatma (Anti-aliasing) açar, çizgiler tırtıklı görünmez
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Gökyüzünü Çiz (Maviden beyaza geçişli renk)
        GradientPaint gp = new GradientPaint(0, 0, new Color(135, 206, 235), 0, 600, Color.WHITE);
        g2.setPaint(gp);
        g2.fillRect(0, 0, 800, 600); // Tüm ekranı boya

        // 2. Tüm Nesneleri Çiz (Döngü ile listeleri gez)
        for (GroundBlock b : zeminBloklari) b.draw(g2); // Binaları çiz
        for (WaterOrb o : suBalonlari) o.draw(g2);      // Balonları çiz
        for (WaterDrop w : atilanSular) w.draw(g2);     // Düşen suları çiz

        // 3. Uçağı Çiz
        // Eğer uçak su taşıyorsa etrafında mavi bir hare oluştur
        if (eldekiSayi > 0) {
            g2.setColor(new Color(0, 100, 255, 60)); // Şeffaf mavi
            g2.fillOval(oyuncu.x - 10, oyuncu.y - 10, 80, 50); // Uçağa göre ayarlandı
        }
        oyuncu.draw(g2); // Uçağın kendi çizim metodunu çağır

        // 4. Arayüz Yazılarını (HUD) Çiz
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("SCORE: " + toplamSkor + " / " + HEDEF_SKOR, 20, 30);

        // Yıkılan bina sayısını kırmızı yaz
        g2.setColor(new Color(192, 57, 43));
        g2.drawString("DESTROYED: " + yikilanBinaSayisi + " / " + MAX_YIKIM, 550, 30);

        // Uçağın durumuna göre bilgi ver
        if (eldekiSayi > 0) {
            g2.setColor(new Color(0, 51, 204));
            g2.drawString("LOADED: " + eldekiSayi, 20, 55); // "Yüklendi"
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("(Click to Drop)", 20, 70); // "Tıkla Bırak"
        } else {
            g2.setColor(Color.RED);
            g2.drawString("CATCH DROPS!", 20, 55); // "Su Yakala"
        }

        // Başarı/Hata mesajı varsa göster
        if (feedbackSayaci > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            // Hata ise Kırmızı, Başarı ise Yeşil renk seç
            g2.setColor(feedbackMesaji.contains("LOST") || feedbackMesaji.contains("WRONG") ? Color.RED : new Color(39, 174, 96));
            g2.drawString(feedbackMesaji, 350, 200);
            feedbackSayaci--; // Sayacı azalt (Yazı bir süre sonra kaybolur)
        }

        // Oyun bittiyse son ekranı göster
        if (oyunBitti) cizOyunSonu(g2);
    }

    // Oyun sonu ekranını çizen yardımcı metod
    private void cizOyunSonu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200)); // Yarı saydam siyah perde
        g2.fillRect(0, 0, 800, 600);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.PLAIN, 50));
        g2.drawString(kazandi ? "CITY SAVED!" : "CITY DESTROYED!", 230, 280);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Click to Restart", 320, 330);
    }

    // --- OYUN MANTIĞI (Timer her çalıştığında burası çalışır) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (oyunBitti) return; // Oyun bittiyse hiçbir işlem yapma
        
        // Kazanma ve Kaybetme Kontrolleri
        if (toplamSkor >= HEDEF_SKOR) { oyunBitti = true; kazandi = true; }
        if (yikilanBinaSayisi >= MAX_YIKIM) { oyunBitti = true; kazandi = false; }

        // --- YANGIN ÇIKARMA MANTIĞI ---
        yanginCikmaSayaci++;
        // Sayaç 230 olunca (yaklaşık 4-5 saniye) bir işlem yap
        if (yanginCikmaSayaci > 230) { 
            int index = new Random().nextInt(zeminBloklari.size()); // Rastgele bir bina seç
            GroundBlock b = zeminBloklari.get(index);
            
            if (!b.isBurnt()) { // Bina zaten kül olmadıysa
                if (!b.isOnFire()) {
                    b.ignite(); // Yanmıyorsa yak
                } else {
                    b.growFire(); // Zaten yanıyorsa ateşi büyüt
                    // Eğer ateş büyüyüp son seviyeyi geçerse bina yıkılır
                    if (b.isBurnt()) {
                        yikilanBinaSayisi++;
                        feedbackMesaji = "BUILDING LOST!";
                        feedbackSayaci = 40;
                    }
                }
            }
            yanginCikmaSayaci = 0; // Sayacı sıfırla
        }

        // --- SU BALONU OLUŞTURMA (SPAWN) ---
        suCikmaSayaci++;
        // Ekranda en fazla 5 balon olsun ve belirli süre geçsin
        if (suCikmaSayaci > 50 && suBalonlari.size() < 5) { 
            // Yanan binaların ihtiyaç duyduğu cevapları bul
            ArrayList<Integer> ihtiyaclar = new ArrayList<>();
            for (GroundBlock b : zeminBloklari) {
                if (b.isOnFire() && !b.isBurnt()) ihtiyaclar.add(b.getAnswer());
            }

            // Eğer yangın varsa, rastgele birinin cevabını taşıyan balon üret
            if (!ihtiyaclar.isEmpty()) {
                int secilenCevap = ihtiyaclar.get(new Random().nextInt(ihtiyaclar.size()));
                // Balonu ekranın üstünden (-50 y koordinatı) başlat
                suBalonlari.add(new WaterOrb(new Random().nextInt(750), -50, secilenCevap));
                suCikmaSayaci = 0;
            }
        }

        // --- HAREKET VE ÇARPIŞMA KONTROLLERİ ---
        
        // Iterator kullanıyoruz çünkü döngü içinde listeden eleman silmek gerekebilir
        Iterator<WaterOrb> itOrb = suBalonlari.iterator();
        while (itOrb.hasNext()) {
            WaterOrb o = itOrb.next();
            o.move(); // Balonu hareket ettir
            
            // Eğer uçakla çarpışırsa VE elimiz boşsa
            if (o.getBounds().intersects(oyuncu.getBounds()) && eldekiSayi == 0) {
                eldekiSayi = o.getVal(); // Suyu al
                itOrb.remove(); // Balonu ekrandan sil
            } else if (o.y > 650) { // Ekranın altına düştüyse
                itOrb.remove(); // Sil
            }
        }

        // Atılan suların hareketi
        Iterator<WaterDrop> itDrop = atilanSular.iterator();
        while (itDrop.hasNext()) {
            WaterDrop w = itDrop.next();
            w.move();
            
            // Su yere (y > 480) ulaştı mı?
            if (w.y > 480) { 
                // Hangi binaya denk geldiğini bul
                for (GroundBlock b : zeminBloklari) {
                    // Koordinat kontrolü (suyun x'i binanın sınırları içinde mi?)
                    if (w.x > b.x && w.x < b.x + 40 && b.isOnFire() && !b.isBurnt()) {
                        // Matematik cevabı doğru mu?
                        if (w.getVal() == b.getAnswer()) {
                            b.extinguish(); // Söndür
                            toplamSkor += 20;
                            feedbackMesaji = "SAVED!";
                        } else { // Yanlış cevap
                            b.growFire(); // Ateş büyür
                            if(b.isBurnt()) { // Yıkıldı mı?
                                yikilanBinaSayisi++;
                                feedbackMesaji = "BUILDING LOST!";
                            } else {
                                feedbackMesaji = "WRONG!";
                            }
                        }
                        feedbackSayaci = 30;
                        break; // Döngüden çık (bir su bir binayı etkiler)
                    }
                }
                itDrop.remove(); // Suyu sil (yere çarptı)
            }
        }
        repaint(); // Ekranı yeniden çiz (PaintComponent'i tetikler)
    }

    // --- MOUSE KONTROLLERİ ---
    // Mouse hareket edince uçağı oraya taşı
    @Override
    public void mouseMoved(MouseEvent e) {
        if (!oyunBitti) {
            // Mouse'un ortasına gelsin diye koordinatları ayarlıyoruz
            oyuncu.x = e.getX() - 30; 
            oyuncu.y = e.getY() - 15;
        }
    }
    
    // Mouse'a tıklayınca
    @Override
    public void mousePressed(MouseEvent e) {
        if (oyunBitti) {
            oyunuBaslat(); // Oyun bittiyse yeniden başlat
        } else if (eldekiSayi > 0) {
            // Elimizde su varsa aşağı bırak
            atilanSular.add(new WaterDrop(oyuncu.x + 30, oyuncu.y + 15, eldekiSayi));
            eldekiSayi = 0; // Elimizi boşalt
        }
    }

    // Kullanılmayan ama zorunlu Mouse metodları (Boş bırakıyoruz)
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // Programın başladığı yer (Main Metodu)
    public static void main(String[] args) {
        JFrame f = new JFrame("EcoWorld – Fire Rescue Math Game"); // Pencere oluştur
        f.add(new Ecoworld()); // Bizim paneli içine ekle
        f.setSize(800, 600); // Boyut
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Çarpıya basınca kapat
        f.setResizable(false); // Boyutlandırmayı kilitle
        f.setLocationRelativeTo(null); // Ekranın ortasında aç
        f.setVisible(true); // Görünür yap
    }
}

// --- YARDIMCI SINIFLAR (NESNELERİMİZ) ---

// Uçak Sınıfı
class Plane {
    int x, y;
    // DEĞİŞİKLİK: Uçak boyutlarını küçülttüm
    Plane(int x, int y) { this.x = x; this.y = y; }
    // Çarpışma kutusu (Hitbox) - Çarpışmaları algılamak için görünmez dikdörtgen
    Rectangle getBounds() { return new Rectangle(x, y, 60, 30); }
    
    void draw(Graphics2D g2) {
        // Uçağı parçalar halinde çiziyoruz (Gövde, Kanat, Cam, Kuyruk)
        g2.setColor(Color.WHITE); g2.fillOval(x, y, 60, 30); // Gövde (Küçüldü: 80->60)
        g2.setColor(Color.RED); g2.fillOval(x + 22, y - 8, 15, 45); // Kanat (Ayartlandı)
        g2.setColor(Color.CYAN); g2.fillOval(x + 45, y + 4, 10, 10); // Cam
        g2.setColor(Color.GRAY); g2.fillOval(x - 5, y + 8, 8, 12); // Kuyruk
    }
}

// Havada süzülen su balonu sınıfı
class WaterOrb {
    int x, y, val;
    WaterOrb(int x, int y, int val) { this.x = x; this.y = y; this.val = val; }
    
    // Balonun hızı: y her seferinde 2 artar 
    void move() { y += 2; } 
    
    Rectangle getBounds() { return new Rectangle(x, y, 30, 30); }
    int getVal() { return val; }
    
    void draw(Graphics2D g2) {
        // Yarı saydam mavi balon çizimi
        g2.setColor(new Color(135, 206, 250, 150)); 
        g2.fillOval(x, y, 30, 30);
        g2.setColor(Color.BLUE); // Çerçeve
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, 30, 30);
        // İçindeki sayıyı yaz
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("" + val, x + 8, y + 22);
    }
}

// Aşağı düşen su damlası sınıfı
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

// Zemin blokları (Evler ve Ağaçlar) sınıfı
class GroundBlock {
    int x, y = 480; // Yerin konumu sabit
    private int type; // 0: Ev, 1: Ağaç
    private int fireLevel = 0; // Yangın seviyesi (0:Yok, 1-3: Yanıyor, 4: Yıkıldı)
    private String question = ""; // Matematik sorusu metni
    private int answer = 0; // Cevap

    GroundBlock(int x) {
        this.x = x;
        this.type = new Random().nextInt(2); // Rastgele ev veya ağaç seç
    }

    // Yangın başlatma metodu
    void ignite() {
        if (fireLevel == 0) {
            fireLevel = 1; // Küçük ateşle başla
            generateMath(); // Yeni soru üret
        }
    }
    
    // Yangını büyütme metodu
    void growFire() {
        if (fireLevel > 0 && fireLevel < 4) fireLevel++;
    }

    // Söndürme metodu
    void extinguish() {
        fireLevel = 0;
    }
    
    // Durum kontrol metodları
    boolean isOnFire() { return fireLevel > 0 && fireLevel < 4; }
    boolean isBurnt() { return fireLevel == 4; }
    int getAnswer() { return answer; }

    // Basit matematik sorusu üreten metod
    void generateMath() {
        Random r = new Random();
        int a = r.nextInt(9) + 1; // 1-9 arası sayı
        int b = r.nextInt(8) + 1;
        // %50 ihtimalle toplama veya çıkarma sor
        if (r.nextBoolean()) { question = a + "+" + b; answer = a + b; } 
        else { question = (a + b) + "-" + a; answer = b; }
    }

    // Bloğu çizen metod
    void draw(Graphics2D g2) {
        // Eğer bina yıkılmışsa gri harabe çiz ve çık
        if (fireLevel == 4) {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x + 5, y + 80, 30, 40); 
            return; 
        }

        // Ev veya Ağaç çizimi
        if (type == 0) { // EV
            g2.setColor(new Color(149, 165, 166)); g2.fillRect(x + 2, y + 20, 36, 100); 
            g2.setColor(Color.YELLOW);
            // Pencereleri çiz
            for(int i = 0; i < 4; i++) { 
                g2.fillRect(x + 8, y + 30 + (i * 20), 8, 8); 
                g2.fillRect(x + 24, y + 30 + (i * 20), 8, 8); 
            }
        } else { // AĞAÇ
            g2.setColor(new Color(121, 85, 72)); g2.fillRect(x + 15, y + 60, 10, 60); // Gövde
            g2.setColor(new Color(39, 174, 96)); g2.fillOval(x + 5, y + 20, 30, 50); // Yapraklar
        }

        // Eğer yanıyorsa üzerine alev ve soru çiz
        if (isOnFire()) {
            // Ateş boyutunu seviyeye göre ayarla
            int size = (fireLevel == 1) ? 20 : (fireLevel == 2) ? 35 : 55; 
            int offsetY = (fireLevel == 1) ? 0 : (fireLevel == 2) ? -10 : -25;
            
            // Titreşen ateş efekti
            Random r = new Random();
            int flicker = r.nextInt(5);
            
            // Kırmızı dış alev
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