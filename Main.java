import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

interface LibraryOperation {
    boolean pinjam(Anggota anggota);
    boolean kembalikan();
    double hitungDenda(LocalDate tanggalKembali);
}

abstract class Koleksi implements LibraryOperation {
    protected String judul;
    protected String penerbit;
    protected int tahunTerbit;
    protected boolean sedangDipinjam;
    protected Anggota anggotaPeminjam;
    protected LocalDate tanggalPinjam;

    public Koleksi(String judul, String penerbit, int tahunTerbit) {
        this.judul = judul;
        this.penerbit = penerbit;
        this.tahunTerbit = tahunTerbit;
        this.sedangDipinjam = false;
    }

    public abstract String getKategori();

    @Override
    public boolean pinjam(Anggota anggota) {
        if(!sedangDipinjam) {
            this.sedangDipinjam = true;
            this.anggotaPeminjam = anggota;
            this.tanggalPinjam = LocalDate.now();
            anggota.pinjamKoleksi(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean kembalikan() {
        if(sedangDipinjam) {
            this.sedangDipinjam = false;
            this.anggotaPeminjam = null;
            return true;
        }
        return false;
    }

    @Override
    public double hitungDenda(LocalDate tanggalKembali) {
        if(tanggalKembali.isAfter(tanggalPinjam.plusDays(7))) {
            long Keterlambatan = ChronoUnit.DAYS.between(tanggalPinjam.plusDays(7), tanggalKembali);
            return Keterlambatan * 1000.0;
        }
        return 0.0;
    }

    public abstract String getDetailInfo();
}

class Buku extends Koleksi {
    private String penulis;
    private int jumlahHalaman;

    public Buku(String judul, String penerbit, int tahunTerbit, String penulis, int jumlahHalaman) {
        super(judul, penerbit, tahunTerbit);
        this.penulis = penulis;
        this.jumlahHalaman = jumlahHalaman;
    }

    @Override
    public String getKategori() {
        return "Buku";
    }

    @Override
    public String getDetailInfo() {
        return String.format("Buku: %s oleh %s (%d), Penerbit: %s, Halaman: %d", 
                             judul, penulis, tahunTerbit, penerbit, jumlahHalaman);
    }
}

class Majalah extends Koleksi {
    private String edisi;
    private String frekuensiTerbit;

    public Majalah(String judul, String penerbit, int tahunTerbit, String edisi, String frekuensiTerbit) {
        super(judul, penerbit, tahunTerbit);
        this.edisi = edisi;
        this.frekuensiTerbit = frekuensiTerbit;
    }

    @Override
    public String getKategori() {
        return "Majalah";
    }

    @Override
    public String getDetailInfo() {
        return String.format("Majalah: %s, Edisi: %s, Penerbit: %s, Frekuensi: %s",
                             judul, edisi, penerbit, frekuensiTerbit);
    }
}

class Jurnal extends Koleksi {
    private String bidangIlmu;
    private double faktorDampak;

    public Jurnal(String judul, String penerbit, int tahunTerbit, String bidangIlmu, double faktorDampak) {
        super(judul, penerbit, tahunTerbit);
        this.bidangIlmu = bidangIlmu;
        this.faktorDampak = faktorDampak;
    }

    @Override
    public String getKategori() {
        return "Jurnal";
    }

    @Override
    public String getDetailInfo() {
        return String.format("Jurnal: %s, Bidang : %s (%d), Penerbit: %s, Faktor Dampak: %.2f",
                             judul, bidangIlmu, tahunTerbit, penerbit, faktorDampak);
    }
}

class KoleksiDigital extends Koleksi {
    private String formatFile;
    private long ukuranFile; //ini dalam kb (bukan keluarga berencana)

    public KoleksiDigital(String judul, String penerbit, int tahunTerbit, String formatFile, long ukuranFile) {
        super(judul, penerbit, tahunTerbit);
        this.formatFile = formatFile;
        this.ukuranFile = ukuranFile;
    }

    @Override
    public String getKategori() {
        return "Koleksi Digital";
    }

    @Override
    public String getDetailInfo() {
        return String.format("Koleksi Digital: %s (%d), Format: %s, Ukuran: %d KB",
                             judul, tahunTerbit, formatFile, ukuranFile);
    }
}

class Anggota {
    private String id;
    private String nama;
    private List<Koleksi> daftarPinjaman;

    public Anggota(String id, String nama) {
        this.id = id;
        this.nama = nama;
        this.daftarPinjaman = new ArrayList<>();
    }

    public void pinjamKoleksi(Koleksi koleksi) {
        daftarPinjaman.add(koleksi);
    }

    public boolean kembalikanKoleksi(Koleksi koleksi) {
        return daftarPinjaman.remove(koleksi);
    }

    public List<Koleksi> getDaftarPinjaman() {
        return daftarPinjaman;
    }

    public String getDetailAnggota() {
        return String.format("Anggota: %s (ID: %s), Koleksi Dipinjam: %d", nama, id, daftarPinjaman.size());
    }
}

class Transaksi {
    private Anggota anggota;
    private Koleksi koleksi;
    private LocalDate tanggalPeminjaman;
    private LocalDate tanggalPengembalian;
    private double denda;

    public Transaksi(Anggota anggota, Koleksi koleksi) {
        this.anggota = anggota;
        this.koleksi = koleksi;
        this.tanggalPeminjaman = LocalDate.now();
    }

    public void selesaikanTransaksi(LocalDate tanggalPengembalian) {
        this.tanggalPengembalian = tanggalPengembalian;
        this.denda = koleksi.hitungDenda(tanggalPengembalian);
    }

    public String getDetailTransaksi() {
        return String.format("Transaksi: %s meminjam %s, Pinjam: %s, Kembali: %s, Denda: Rp%.2f", 
                                anggota.getDetailAnggota(), koleksi.getDetailInfo(), tanggalPeminjaman, tanggalPengembalian, denda);
    }
}

class Perpustakaan {
    private List<Koleksi> daftarKoleksi;
    private List<Anggota> daftarAnggota;
    private List<Transaksi> daftarTransaksi;

    public Perpustakaan() {
        daftarKoleksi = new ArrayList<>();
        daftarAnggota = new ArrayList<>();
        daftarTransaksi = new ArrayList<>();
    }

    public void cetakInformasiLengkapKoleksi() {
        System.out.println("\n==== INFORMASI LENGKAP KOLEKSI ====");
        for(Koleksi koleksi : daftarKoleksi) {
            System.out.println(koleksi.getDetailInfo());
            System.out.println("Status: " + (koleksi.sedangDipinjam ? "Sedang Dipinjam" : "Tersedia"));
            System.out.println("-------------------------------");
        }
    }

    public void cetakAktivitasPeminjaman() {
        System.out.println("\n==== AKTIVITAS PEMINJAMAN ====");
        for(Transaksi transaksi : daftarTransaksi) {
            System.out.println(transaksi.getDetailTransaksi());
            System.out.println("----------------------------");
        }
    }

    public void hitungDanCetakDendaAnggota() {
        System.out.println("\n==== DENDA ANGGOTA ====");
        for(Anggota anggota : daftarAnggota) {
            double totalDenda = 0;
            System.out.println("Anggota: " + anggota.getDetailAnggota());

            for(Koleksi koleksi : anggota.getDaftarPinjaman()) {
                if(koleksi.sedangDipinjam) {
                    double denda = koleksi.hitungDenda(LocalDate.now());
                    if(denda > 0) {
                        System.out.printf("Koleksi: %s, Denda: Rp%.2f%n", koleksi.judul, denda);
                        totalDenda += denda;
                    }
                }
            }
            System.out.printf("Total Denda: Rp%.2f%n", totalDenda);
        }
    }

    public void cetakTransaksiAnggota(Anggota anggota) {
        System.out.println("\n==== TRANSAKSI ANGGOTA: " + anggota.getDetailAnggota() + "====");
        daftarTransaksi.stream()
                .filter(t -> t.getDetailTransaksi().contains(anggota.getDetailAnggota()))
                .forEach(t -> {
                    System.out.println(t.getDetailTransaksi());
                    System.out.println("--------------------");
                });
    }

    public void tambahKoleksi(Koleksi koleksi) {
        daftarKoleksi.add(koleksi);
    }

    public void tambahAnggota(Anggota anggota) {
        daftarAnggota.add(anggota);
    }

    public List<Koleksi> cariKoleksi(String judul) {
        return daftarKoleksi.stream()
                            .filter(koleksi -> koleksi.judul.toLowerCase().contains(judul.toLowerCase()))
                            .collect(Collectors.toList());
    }

    public List<Koleksi> urutkanKoleksi() {
        return daftarKoleksi.stream()
                            .sorted(Comparator.comparingInt(k -> k.tahunTerbit))
                            .collect(Collectors.toList());
    }

    public void cetakStatistikKoleksi() {
        System.out.println("Statistik Koleksi Perpustakaan: ");
        daftarKoleksi.stream()
                    .collect(Collectors.groupingBy(Koleksi::getKategori, Collectors.counting()))
                    .forEach((kategori, jumlah) -> System.out.println(kategori + ": " + jumlah));
    }

    public boolean pinjamKoleksi(Anggota anggota, Koleksi koleksi) {
        if(koleksi.pinjam(anggota)) {
            Transaksi transaksi = new Transaksi(anggota, koleksi);
            daftarTransaksi.add(transaksi);
            return true;
        }
        return false;
    }

    public boolean kembalikanKoleksi(Anggota anggota, Koleksi koleksi) {
        if(koleksi.kembalikan() && anggota.kembalikanKoleksi(koleksi)) {
            daftarTransaksi.stream()
                        .filter(t -> t.getDetailTransaksi().contains(koleksi.judul))
                        .findFirst()
                        .ifPresent(t -> t.selesaikanTransaksi(LocalDate.now()));
            return true;
        }
        return false;
    }
}

public class Main {
    public static void main(String[] args) {
        Perpustakaan perpustakaan = new Perpustakaan();
        
        //koleksi koleksinya dsni
        Buku buku1 = new Buku("Laskar Pelangi", "Benteng Pustaka", 2005, "Andrea Hirata", 372);
        Majalah majalah1 = new Majalah("National Geographic", "Gramedia", 2023, "Juli", "Bulanan");
        Jurnal jurnal1 = new Jurnal("Jurnal Teknologi Informasi", "ITB Press", 2022, "Informatika", 4.5);
        KoleksiDigital digital1 = new KoleksiDigital("Ebook Pemrograman Java", "Penerbit Online", 2023, "PDF", 5210);

        //masukkan ke perpustakaan
        perpustakaan.tambahKoleksi(buku1);
        perpustakaan.tambahKoleksi(majalah1);
        perpustakaan.tambahKoleksi(jurnal1);
        perpustakaan.tambahKoleksi(digital1);

        //buat anggotanya juga dsni
        Anggota anggota1 = new Anggota("42623002", "Raffi Fadlika");
        Anggota anggota2 = new Anggota("42623024", "Asti Hafsari");
        perpustakaan.tambahAnggota(anggota1);
        perpustakaan.tambahAnggota(anggota2);

        //simulasi keterlambatan
        System.out.println("==== PROSES PEMINJAMAN ====");

        //proses peminjaman
        perpustakaan.pinjamKoleksi(anggota1, buku1);
        System.out.println("Raffi meminjam buku Laskar Pelangi");

        perpustakaan.pinjamKoleksi(anggota2, digital1);
        System.out.println("Asti meminjam Koleksi Digital Ebook Pemrograman Java");

        //cetak cetakan
        perpustakaan.cetakInformasiLengkapKoleksi();
        perpustakaan.cetakAktivitasPeminjaman();

        //simulasi keterlambatan
        System.out.println("\n==== SIMULASI KETERLAMBATAN ====");
        buku1.tanggalPinjam = LocalDate.now().minusDays(10); //peminjaman 10 hari yang lalu
        perpustakaan.hitungDanCetakDendaAnggota();

        perpustakaan.cetakTransaksiAnggota(anggota1);
    }
}