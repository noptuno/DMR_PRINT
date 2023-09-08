package com.gpp.dmr_print;

import static print.Print.WriteData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelUuid;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bixolon.commonlib.BXLCommonConst;
import com.bixolon.commonlib.emul.image.LabelImage;
import com.bixolon.labelprinter.BixolonLabelPrinter;
import com.bixolon.labelprinter.service.ServiceManager;


import com.esc.PrinterDataCore;
import com.example.tscdll.TSCActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.gpp.dmr_print.ConfiguracionS.Configuracion_Impresora;
import com.gpp.dmr_print.ConfiguracionS.DMRPrintSettings;

import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscCmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.CompressedBitmapOutputStreamCpcl;
import com.zebra.sdk.graphics.internal.DitheredImageProvider;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.internal.PrinterConnectionOutputStream;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.printer.Document;
import honeywell.printer.DocumentDPL;
import honeywell.printer.DocumentEZ;
import honeywell.printer.DocumentExPCL_LP;
import honeywell.printer.DocumentExPCL_PP;
import honeywell.printer.DocumentLP;
import honeywell.printer.ParametersDPL;
import honeywell.printer.ParametersEZ;
import honeywell.printer.ParametersExPCL_LP;
import honeywell.printer.ParametersExPCL_PP;
import honeywell.printer.configuration.dpl.PrinterInformation_DPL;
import honeywell.printer.configuration.expcl.BatteryCondition_ExPCL;
import honeywell.printer.configuration.expcl.BluetoothConfiguration_ExPCL;
import honeywell.printer.configuration.ez.SerialNumber;
import print.Print;

public class Pdf_view extends AppCompatActivity implements Runnable {

    private boolean gap = false;
    private static final String RECIBIRFOLDER = "ENVIANDO_FOLDER";
    private static final String RECIBIRINTENT = "ENVIANDO_INTENT";
    private Toast toast = null;
    Toolbar toolbar;
    private String m_printerMode;
    private int timeout = 0;
    // public static IDAL idal = null;
    // private IPrinter printer;
    private String m_printerMAC = null;
    private String connectionType;
    private int m_printHeadWidth = 384;
    private int cantidaddecopias = 0;
    public int imprimirautomaticamente = 0;
    private int minimizarapp = 0;
    private Boolean ConfiguracionImpresora = false;

    //se configuro la impresora?

    private Document doct;
    private DocumentEZ docEZ;
    private DocumentLP docLP;
    private DocumentDPL docDPL;
    private DocumentExPCL_LP docExPCL_LP;
    private DocumentExPCL_PP docExPCL_PP;
    private ParametersEZ paramEZ;
    private ParametersDPL paramDPL;
    private ParametersExPCL_LP paramExPCL_LP;
    private ParametersExPCL_PP paramExPCL_PP;
    byte[] printData = {0};
    byte[] printBitmap = {0};
    private Bitmap mBitmap;
    private Handler m_handler = new Handler(); // Main thread
    private ProgressDialog dialog;
    ConnectionBase conn = null;
    String ApplicationConfigFilename = "applicationconfigg.dat";
    private boolean bandera = true;
    private int contador = 0;
    private PDFView pdfView;
    private File myFile = null;
    TSCActivity TscDll = new TSCActivity();
    DMRPrintSettings g_appSettings = new DMRPrintSettings("", "", 0, "/", "", 0, 0, 0, 0, 0, 0, 0, "", "", false, 0, 10000);
    int heigth_calculator, wigth_calculator, densidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pdfView = findViewById(R.id.pdfView);

        recibir_pdf_mainactivity();

        FloatingActionButton fagimprimir = findViewById(R.id.accion_imprimir);
        fagimprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                preparardocumento(myFile.getAbsolutePath());

            }
        });

        FloatingActionButton fagcomaprtir = findViewById(R.id.accion_compartir);
        fagcomaprtir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myFile != null) {

                    File pdfFile = new File(myFile.getAbsolutePath());
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    Uri uri = FileProvider.getUriForFile(Pdf_view.this, "com.gpp.dmr_print.provider", pdfFile);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        List<ResolveInfo> resInfoList = Pdf_view.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            Pdf_view.this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                    if (intent.resolveActivity(Pdf_view.this.getPackageManager()) != null) {
                        Pdf_view.this.startActivity(intent);
                    }

                }

            }
        });

    }

    private void recibir_pdf_mainactivity() {

        Intent intentrecibir = getIntent();

        if (intentrecibir != null) {

            if (intentrecibir.getAction().equals(RECIBIRFOLDER)) {

                try {
                    String path = intentrecibir.getExtras().getString("PATH");
                    myFile = new File(path);
                    AbriPdf(myFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (intentrecibir.getAction().equals(RECIBIRINTENT)) {
                myFile = (File) intentrecibir.getSerializableExtra("MY_FILE");
                AbriPdf(myFile);
            } else {
                Toast.makeText(Pdf_view.this, "No se de donde recibe", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        Cargar_Datos_Configuracion();
    }


    public void AbriPdf(File bFileeee) {

        if (bFileeee.length() > 0) {
            pdfView.fromFile(bFileeee).defaultPage(0).onLoad(new OnLoadCompleteListener() {
                @Override
                public void loadComplete(int nbPages) {

                }
            }).scrollHandle(new DefaultScrollHandle(Pdf_view.this)).load();
        } else {
            Toast.makeText(Pdf_view.this, "Arhivo Dañado", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        killToast();
        super.onPause();
    }

    private void killToast() {
        if (this.toast != null) {
            this.toast.cancel();
        }
    }

    private void showToast(String message) {
        if (this.toast == null) {
            // Create toast if found null, it would he the case of first call only
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else if (this.toast.getView() == null) {
            // Toast not showing, so create new one
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else {
            // Updating toast message is showing
            this.toast.setText(message);
        }

        // Showing toast finally
        this.toast.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_print, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            Intent v = new Intent(Pdf_view.this, Configuracion_Impresora.class);
            startActivity(v);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void Cargar_Datos_Configuracion() {

        DMRPrintSettings appSettings = ReadApplicationSettingFromFile();
        if (appSettings != null) {
            g_appSettings = appSettings;
            m_printerMode = g_appSettings.getSelectedPrintMode();
            m_printerMAC = g_appSettings.getPrinterMAC();// mac de la impresora
            m_printHeadWidth = g_appSettings.getPrintheadWidth();// tamaño del ancho de impredion
            connectionType = g_appSettings.getCommunicationType(); // tipo de conexion bluetooth o tcpip
            cantidaddecopias = g_appSettings.getSelecciondehojas();// cantidad de copias para imprimir
            minimizarapp = g_appSettings.getMinimizar();//minimizar app
            imprimirautomaticamente = g_appSettings.getAutomatico();// imprimir automaticamente
            ConfiguracionImpresora = g_appSettings.getEstadoconfiguracion();
            densidad = g_appSettings.getDensidad();
            timeout = g_appSettings.getTimeout();
        } else {
            ConfiguracionImpresora = false;
            m_printerMode = "";
            m_printerMAC = "";// mac de la impresora
            m_printHeadWidth = 0;// tamaño del ancho de impredion
            connectionType = ""; // tipo de conexion bluetooth o tcpip
            cantidaddecopias = 0;// cantidad de copias para imprimir
            minimizarapp = 0;//minimizar app
            imprimirautomaticamente = 0;// imprimir automaticamente
            densidad = 0;

            showToast("No se ha Configurado la impresora");
        }

    }

    DMRPrintSettings ReadApplicationSettingFromFile() {
        DMRPrintSettings ret = null;
        InputStream instream;
        try {
            instream = openFileInput(ApplicationConfigFilename);
        } catch (FileNotFoundException e) {
            showToast("Cargar su Configuracion...");
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(instream);
            try {
                ret = (DMRPrintSettings) ois.readObject();
            } catch (ClassNotFoundException e) {
                ret = null;
            }
        } catch (Exception e) {
            ret = null;
        } finally {
            try {
                if (instream != null)
                    instream.close();
            } catch (IOException ignored) {
            }
        }
        return ret;
    }


    private void preparardocumento(String ubicacionarchivo) {

        try {

            docDPL = new DocumentDPL();
            docExPCL_LP = new DocumentExPCL_LP(3);
            paramDPL = new ParametersDPL();
            paramExPCL_LP = new ParametersExPCL_LP();
            printData = new byte[]{0};
            // ICommandBuilder builder;
            switch (m_printerMode) {

                case "Apex":
                    docExPCL_LP.writePDF(ubicacionarchivo, m_printHeadWidth);
                    docExPCL_LP.writeText("", paramExPCL_LP);
                    docExPCL_LP.writeText("", paramExPCL_LP);
                    docExPCL_LP.writeText("", paramExPCL_LP);
                    docExPCL_LP.writeText("", paramExPCL_LP);
                    docExPCL_LP.writeText("", paramExPCL_LP);
                    printData = docExPCL_LP.getDocumentData();
                    //Toast.makeText(Pdf_view.this, "B", Toast.LENGTH_SHORT).show();
                    break;

                case "DPL":

                    docDPL.writePDF(ubicacionarchivo, m_printHeadWidth, 0, 0);
                    docDPL.writeLine(0, 0, 10, 25);
                    docDPL.writeLine(0, 0, 10, 25);
                    docDPL.writeLine(0, 0, 10, 25);
                    printData = docDPL.getDocumentData();
                    break;

                case "StarPRNT":
/*
                    mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                    if (mBitmap != null) {
                        builder = StarIoExt.createCommandBuilder(StarIoExt.Emulation.StarPRNT);
                        byte[] bitmapData = convertTo1BPP(mBitmap, 128);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                        builder.beginDocument();
                        builder.appendPeripheral(ICommandBuilder.PeripheralChannel.No1);
                        builder.appendBitmap(bitmap, false, m_printHeadWidth, true);
                        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
                        builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
                        builder.endDocument();
                        printData = builder.getCommands();


                    } else {
                        printData = new byte[0];
                    }

                    */
                    break;

                case "EscPosMobile":
/*
                    //  Drawable drawable = getResources().getDrawable(R.drawable.dologo);
                    //   mBitmap = ((BitmapDrawable)drawable).getBitmap();
                    mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                    if (mBitmap != null) {
                        builder = StarIoExt.createCommandBuilder(StarIoExt.Emulation.EscPosMobile);
                        byte[] bitmapData = convertTo1BPP(mBitmap, 128);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                        builder.beginDocument();
                        builder.appendPeripheral(ICommandBuilder.PeripheralChannel.No1);
                        builder.appendBitmap(bitmap, false, m_printHeadWidth, true);
                        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
                        builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
                        builder.endDocument();
                        printData = builder.getCommands();
                    } else {
                        printData = new byte[0];
                    }

                    */
                    break;

                case "CPCL":

                    mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                    if (mBitmap != null) {
                        byte[] bitmapData = convertTo1BPP(mBitmap, 128);
                        Bitmap bitt = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                        printPhotoFromExternal(bitt);
                    }

                    break;

                    /*
                case "PAX":
                                        cargarIDALPax();
                    if (pax){
                        mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                        if (mBitmap != null) {
                            byte[] bitmapData = convertTo1BPP(mBitmap, 128);
                            Bitmap bitt = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);

                            PrintPax(bitt);
                        }
                    }


                                        break;
                    */

                case "TSC":

                    try {
                        mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                        heigth_calculator = (int) (mBitmap.getHeight() / 8);
                        wigth_calculator = (int) (mBitmap.getWidth() / 8);

                        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/" + "/temp2.BMP");
                        byte[] bitmapData2 = convertTo1BPP(mBitmap, 128);
                        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData2);

                        InputStream is = bs;
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(buffer);
                        fos.close();

                        int a = mBitmap != null ? mBitmap.getHeight() : 0;
                        Log.e("TAMAÑO"," " + a);


                        PrintBmpTsc();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;


                case "ESCPOS":
                    new Thread(new Runnable() {
                        public void run() {

                            EnableDialog(true, "Preparando Documento...", true);

                            try {

                            CmdFactory escFac = new EscFactory();
                            Cmd escCmd = escFac.create();

                            escCmd.append(escCmd.getHeaderCmd());

                            escCmd.setChartsetName("UTF-8");

                            CommonSetting commonSetting = new CommonSetting();
                            commonSetting.setAlign(CommonEnum.ALIGN_LEFT);
                            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));


                            BitmapSetting bitmapSetting = new BitmapSetting();
                            bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                            Bitmap mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                            bitmapSetting.setBimtapLimitWidth(72 * 8);

                            escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));

                            escCmd.append(escCmd.getCRCmd());
                            escCmd.append(escCmd.getCRCmd());
                            escCmd.append(escCmd.getCRCmd());
                            escCmd.append(escCmd.getCRCmd());
                            escCmd.append(escCmd.getCRCmd());
                            escCmd.append(escCmd.getCRCmd());

                            escCmd.append(escCmd.getLFCRCmd());
                            escCmd.append(escCmd.getLFCRCmd());
                            escCmd.append(escCmd.getLFCRCmd());
                            escCmd.append(escCmd.getLFCRCmd());
                            escCmd.append(escCmd.getLFCRCmd());
                            escCmd.append(escCmd.getLFCRCmd());

                            escCmd.append(escCmd.getEndCmd());
                            printBitmap = escCmd.getAppendCmds();

                            int a = mBitmap != null ? mBitmap.getHeight() : 0;
                            Log.e("TAMAÑO BITMAP"," " + a);

                            if  (timeout == 0){
                                timeout = a * 14;
                            }
                                EnableDialog(false, "Enviando terminando...", false);

                                Log.e("TAMAÑO seleccionado"," " + timeout);
                                printESCPOSRUNTSC();

                            } catch (SdkException e) {

                                EnableDialog(false, "Enviando terminando...", false);
                                throw new RuntimeException(e);
                            }



                        }
                    }).start();





                    //printEscposTestRunservice(ubicacionarchivo);

               // printescposHprt(ubicacionarchivo);



                    break;

                case "BIXOLON":

                    mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                    if (mBitmap != null) {
                        byte[] bitmapData = convertTo1BPP(mBitmap, 128);
                        Bitmap bitt = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                        printBitmapBixolon(bitt);
                    }

            }

            if ((m_printerMode.equals("Apex") || m_printerMode.equals("DPL"))) {
                new Thread(Pdf_view.this, "PrintingTask").start();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printEscposTestRunservice(String a) throws SdkException {

        mServiceManager = new ServiceManager(getApplicationContext(), mHandlerer,  Looper.getMainLooper());

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    EnableDialog(true, "Imprimiendo test de prueba", true);

                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();
                    escCmd.append(escCmd.getHeaderCmd());
                    escCmd.setChartsetName("UTF-8");
                    CommonSetting commonSetting = new CommonSetting();
                    commonSetting.setAlign(CommonEnum.ALIGN_LEFT);
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
                    BitmapSetting bitmapSetting = new BitmapSetting();
                    bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                    Bitmap mBitmap = generateImageFromPdf(a, 0, m_printHeadWidth);
                    bitmapSetting.setBimtapLimitWidth(72 * 8);
                    escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getEndCmd());
                    escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getEndCmd());

                    printBitmap = escCmd.getAppendCmds();

                    mServiceManager.connect(m_printerMAC, BXLCommonConst._PORT_BLUETOOTH);
                    mServiceManager.Write(printBitmap);
                    //mServiceManager.executeCommand("P" + 1 + "," + 1, false);


                    EnableDialog(false, "Imprimiendo test de prueba", false);

                } catch (Exception e) {
                    e.printStackTrace();
                    EnableDialog(false, "Imprimiendo test de prueba", false);
                }
            }
        };
        thread.start();

    }


    private void conversorESCPOS(Bitmap bmp, byte var1, byte var2, int var3) {

    }


    private void printescposHprt(String ubicacionarchivo) {



        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    final int result = Print.PortOpen(getApplicationContext(), "Bluetooth," + m_printerMAC);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result == 0){
                                Log.e("bluettoh","conecto");
                                mBitmap = generateImageFromPdf(ubicacionarchivo, 0, m_printHeadWidth);
                                try {
                                    int printImage = Print.PrintBitmap(mBitmap, 0, 0);

                                } catch (Exception e) {
                                    Log.e("print bmp","error");
                                    e.printStackTrace();
                                }

                            }else{

                                Log.e("bluettoh","no entro");
                                //  NO CONECTO
                            }

                        }
                    });

                } catch (Exception e) {

                }
            }
        }.start();


    }


    private void printESCPOSRUNTSC() {


        new Thread(new Runnable() {
            public void run() {
                EnableDialog(true, "Enviando Documento...", true);
                if (TscDll.openport(m_printerMAC).equals("1")) {

                    TscDll.sendcommand(printBitmap);

                    if (cantidaddecopias == 1) {
                        Log.e("entro","copia dos enviada");
                        TscDll.sendcommand(printBitmap);
                    }

                    if(TscDll.closeport(timeout).equals("1")){
                        Log.e("cerro","bien");
                    }else{
                        Log.e("cerro","error cerrar");
                    }
                }
                EnableDialog(false, "Enviando terminando...", false);
            }
        }).start();

    }

    private final Handler mHandlerer = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonLabelPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "conectado", Toast.LENGTH_SHORT).show();
                            break;

                        case BixolonLabelPrinter.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "conectando", Toast.LENGTH_SHORT).show();
                            break;

                        case BixolonLabelPrinter.STATE_NONE:
                            Toast.makeText(getApplicationContext(), "Reintentar", Toast.LENGTH_SHORT).show();

                            break;
                    }
                    break;
            }
        }
    };

    private ServiceManager mServiceManager;
    private void printBitmapBixolon(Bitmap bitmap) {
/*
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = 1;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeFile(pathName, opts);
*/

            mServiceManager = new ServiceManager(getApplicationContext(), mHandlerer,  Looper.getMainLooper());

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {

                        EnableDialog(true, "Imprimiendo test de prueba", true);
                        if (bitmap != null) {
                            LabelImage image = new LabelImage();
                            if (image.Load(bitmap)) {
                                image.MakeLD(0, 0, m_printHeadWidth,  0, 0, 50, 30);

                                mServiceManager.connect(m_printerMAC, BXLCommonConst._PORT_BLUETOOTH);
                                mServiceManager.Write(image.PopAll());
                                mServiceManager.executeCommand("P" + 1 + "," + 1, false);

                            }

                        }

                        EnableDialog(false, "Imprimiendo test de prueba", false);

                    } catch (Exception e) {
                        e.printStackTrace();
                        EnableDialog(false, "Imprimiendo test de prueba", false);
                    }
                }
            };
            thread.start();




    }

    private void PrintBmpTsc() {
        

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    EnableDialog(true, "Conectando Impresora...", true);
                    TscDll.openport(m_printerMAC);
                    TscDll.downloadbmp("temp2.BMP");
                    TscDll.setup(wigth_calculator, heigth_calculator, 4, densidad, 0, 0, 0);
                    TscDll.clearbuffer();
                    Log.e("TIMEOUT"," " + timeout);
                    if  (timeout == 0){
                        int a = mBitmap != null ? mBitmap.getHeight() : 0;
                        Log.e("TAMAÑO"," " + a);
                        timeout = a * 14;
                    }


                    TscDll.sendcommand("PUTBMP 10,10,\"temp2.BMP\"\n");
                    TscDll.printlabel(1, 1);
                    TscDll.closeport(timeout);

                    Log.e("DATOS ACTUALES:","Mac: " + m_printerMAC + " Ancho: " + wigth_calculator + " Largo: " + heigth_calculator + "Densidad: " + densidad );

                    EnableDialog(false, "Enviando terminando...", false);

                } catch (Exception e) {
                    e.printStackTrace();
                    EnableDialog(false, "Enviando terminando...", false);
                }


            }
        };

        thread.start();


    }


    /*
    private void PrintPax(final Bitmap bbr) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EnableDialog(true, "Conectando Impresora...", true);

                    try {
                        printer.init();
                    } catch (PrinterDevException e) {
                        e.printStackTrace();
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = true;

                    try {
                        printer.printBitmap(bbr);
                    } catch (PrinterDevException e) {
                        e.printStackTrace();
                    }

                    try {
                        final int status =  printer.start();

                        Log.e("ESTATUS","" + status);
                    } catch (PrinterDevException e) {
                        e.printStackTrace();
                    }



                    EnableDialog(false, "Enviando Documento...", false);

                } catch (Exception e) {

                }
            }
        }).start();
    }
*/


    private void printPhotoFromExternal(final Bitmap bbr) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EnableDialog(true, "Conectando Impresora...", true);
                    Looper.prepare();
                    Connection connection = new BluetoothConnection(m_printerMAC);
                    connection.open();

                    //ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                    //ZebraPrinter  printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL, connection);
                    //PrinterLanguage pclenguaje = printer.getPrinterControlLanguage();
                    //System.out.println("Printer Control Language is " + pclenguaje);
                    //GraphicsUtilCpcll.printImage(new ZebraImageAndroid(bbr), 0, 0, 0, 0, false);

                    ZebraImageAndroid imagenandroid = new ZebraImageAndroid(bbr);

                    int var2 = 0;
                    int var3 = 0;
                    int iamodificado = (imagenandroid.getWidth() + 7) / 8;

                    try {

                        ByteArrayOutputStream var9 = new ByteArrayOutputStream();
                        String var10 = "! 0 200 200 " + imagenandroid.getHeight() + " 1\r\n";
                        String var01 = "CENTER\r\n";
                        String var11 = "PRINT\r\n";

                        var9.write(var10.getBytes());
                        var9.write(var01.getBytes());
                        var9.write("CG ".getBytes());
                        var9.write(String.valueOf(iamodificado).getBytes());
                        var9.write(" ".getBytes());
                        var9.write(String.valueOf(imagenandroid.getHeight()).getBytes());
                        var9.write(" ".getBytes());
                        var9.write(String.valueOf(var2).getBytes());
                        var9.write(" ".getBytes());
                        var9.write(String.valueOf(var3).getBytes());
                        var9.write(" ".getBytes());

                        connection.write(var9.toByteArray());

                        PrinterConnectionOutputStream var12 = new PrinterConnectionOutputStream(connection);
                        CompressedBitmapOutputStreamCpcl var13 = new CompressedBitmapOutputStreamCpcl(var12);
                        DitheredImageProvider.getDitheredImage(imagenandroid, var13);

                        var13.close();
                        var12.close();

                        connection.write("\r\n".getBytes());
                        connection.write(var11.getBytes());

                    } catch (Exception var14) {
                        throw new ConnectionException(var14.getMessage());
                    }

                    connection.close();
                    EnableDialog(false, "Enviando Documento...", false);

                } catch (ConnectionException e) {

                } finally {
                    Looper.myLooper().quit();
                }
            }
        }).start();
    }


    private byte[] intToDWord(int parValue) {
        byte[] retValue = new byte[]{(byte) (parValue & 255), (byte) (parValue >> 8 & 255), (byte) (parValue >> 16 & 255), (byte) (parValue >> 24 & 255)};
        return retValue;
    }

    private byte[] intToWord(int parValue) {
        byte[] retValue = new byte[]{(byte) (parValue & 255), (byte) (parValue >> 8 & 255)};
        return retValue;
    }

    private byte[] convertTo1BPP(Bitmap inputBitmap, int darknessThreshold) {
        int width = inputBitmap.getWidth();
        int height = inputBitmap.getHeight();
        ByteArrayOutputStream mImageStream = new ByteArrayOutputStream();
        int BITMAPFILEHEADER_SIZE = 14;
        int BITMAPINFOHEADER_SIZE = 40;
        short biPlanes = 1;
        short biBitCount = 1;
        int biCompression = 0;
        int biSizeImage = (width * biBitCount + 31 & -32) / 8 * height;
        int biXPelsPerMeter = 0;
        int biYPelsPerMeter = 0;
        int biClrUsed = 2;
        int biClrImportant = 2;
        byte[] bfType = new byte[]{66, 77};
        short bfReserved1 = 0;
        short bfReserved2 = 0;
        int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE + 8;
        int bfSize = bfOffBits + biSizeImage;
        byte[] colorPalette = new byte[]{0, 0, 0, -1, -1, -1, -1, -1};
        int monoBitmapStride = (width + 31 & -32) / 8;
        byte[] newBitmapData = new byte[biSizeImage];

        try {
            mImageStream.write(bfType);
            mImageStream.write(this.intToDWord(bfSize));
            mImageStream.write(this.intToWord(bfReserved1));
            mImageStream.write(this.intToWord(bfReserved2));
            mImageStream.write(this.intToDWord(bfOffBits));
            mImageStream.write(this.intToDWord(BITMAPINFOHEADER_SIZE));
            mImageStream.write(this.intToDWord(width));
            mImageStream.write(this.intToDWord(height));
            mImageStream.write(this.intToWord(biPlanes));
            mImageStream.write(this.intToWord(biBitCount));
            mImageStream.write(this.intToDWord(biCompression));
            mImageStream.write(this.intToDWord(biSizeImage));
            mImageStream.write(this.intToDWord(biXPelsPerMeter));
            mImageStream.write(this.intToDWord(biYPelsPerMeter));
            mImageStream.write(this.intToDWord(biClrUsed));
            mImageStream.write(this.intToDWord(biClrImportant));
            mImageStream.write(colorPalette);
            int[] imageData = new int[height * width];
            inputBitmap.getPixels(imageData, 0, width, 0, 0, width, height);

            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    int pixelIndex = y * width + x;
                    int mask = 128 >> (x & 7);
                    int pixel = imageData[pixelIndex];
                    int R = Color.red(pixel);
                    int G = Color.green(pixel);
                    int B = Color.blue(pixel);
                    int A = Color.alpha(pixel);
                    boolean set = A < darknessThreshold || R + G + B > darknessThreshold * 3;
                    if (set) {
                        int index = (height - y - 1) * monoBitmapStride + (x >>> 3);
                        newBitmapData[index] = (byte) (newBitmapData[index] | mask);
                    }
                }
            }

            mImageStream.write(newBitmapData);
        } catch (Exception var36) {
            var36.printStackTrace();
        }

        return mImageStream.toByteArray();
    }


    private Bitmap generateImageFromPdf(String assetFileName, int pageNumber, int printHeadWidth) {

        PdfiumCore pdfiumCore = new PdfiumCore(Pdf_view.this);
        try {
            File f = new File(assetFileName);
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);

            float scale = (float) printHeadWidth / width;
            float scaledWidth = width * scale;
            float scaledHeight = height * scale;
            Bitmap bmp = Bitmap.createBitmap((int) scaledWidth, (int) scaledHeight, Bitmap.Config.ARGB_8888);
            int width2 = (int) scaledWidth;
            int height2 = (int) scaledHeight;

            pdfiumCore.getClass();
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width2, height2);
            pdfiumCore.closeDocument(pdfDocument);

            return bmp;

        } catch (Exception e) {
            //todo with exception
        }
        return null;
    }


    private void createCancelProgressDialog(String title, String message, String buttonText, Boolean cancelar) {

        dialog = new ProgressDialog(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void EnableDialog(final boolean value, final String mensaje, final boolean cancelar) {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                if (value) {
                    createCancelProgressDialog("Cargando...", mensaje, "Cancelar", cancelar);

                } else {
                    if (dialog != null)
                        dialog.dismiss();
                }
            }
        });
    }

    public void DisplayPrintingStatusMessage(final String MsgStr) {

        m_handler.post(new Runnable() {
            public void run() {
                showToast(MsgStr);//2018 PH
            }// run()
        });
        

    }

    private void testescpos() {

        new Thread(new Runnable() {
            public void run() {
                EnableDialog(true, "Enviando Documento...", true);

                if (TscDll.openport(m_printerMAC).equals("1")){


                    if (TscDll.sendcommand(printData).equals("1")){
                        Log.e("SEND","datos enviados");

                        if (cantidaddecopias==1){
                            if (TscDll.sendcommand(printData).equals("1")){
                                Log.e("SEND 2","datos enviados");
                            }
                        }
                    }
                }

                if(TscDll.closeport().equals("1")){
                    Log.e("CLOSE","Correcto");
                }else{
                    Log.e("CLOSE","InCorrecto");
                }

                EnableDialog(false, "Enviando terminando...", false);
            }
        }).start();
    }


    private void printescptest() {


    }


    private void escposprinttscrun() {

        new Thread(new Runnable() {
            public void run() {
                EnableDialog(true, "Enviando Documento...", true);

              //  btContent = str.getBytes("GBK");

                if (TscDll.openport(m_printerMAC).equals("1")){




                    /*
                     escCmd.append(escCmd.getLFCRCmd());//espacios
                        cmd.append("ESC @".getBytes(StandardCharsets.UTF_8));
                        cmd.append("LF ".getBytes(StandardCharsets.UTF_8));
                        cmd.append("LF ".getBytes(StandardCharsets.UTF_8));
                        cmd.append("LF ".getBytes(StandardCharsets.UTF_8));
                        cmd.append("LF ".getBytes(StandardCharsets.UTF_8));
*/


                      //  printespacio = cmd.getAppendCmds();

                      //  TscDll.sendcommand(btStart);
                      //  TscDll.sendcommand(cmd.getLFCRCmd());


                   // TscDll.sendcommand("ESC @");
               //  TscDll.sendcommand(printData).equals("1"))


                        Log.e("SEND","datos enviados");
/*
                        if (cantidaddecopias==1){
                            if (TscDll.sendcommand(printData).equals("1")){
                                Log.e("SEND 2","datos enviados");
                            }
                         //   TscDll.sendcommand("ESC @");

                        }
                        */

                    }

                if(TscDll.closeport().equals("1")){
                    Log.e("CLOSE","Correcto");
                }else{
                    Log.e("CLOSE","InCorrecto");
                }

                EnableDialog(false, "Enviando terminando...", false);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Ondestroy", "cerro destroy");


    }

    private void runEscPos(Bitmap bmp){

        new Thread(new Runnable() {
            public void run() {
                    try {

                        if (contador > 0) {
                            EnableDialog(true, "Enviando Documento... reintentando", false);
                        } else {
                            EnableDialog(true, "Enviando Documento...", false);
                        }
                        contador++;
                        conn = null;

                        if (connectionType.equals("Bluetooth")) {
                            Looper.prepare();
                            conn = Connection_Bluetooth.createClient(m_printerMAC, false);
                        }

                        if (!conn.getIsOpen()) {
                            if (conn.open()) {

                                int bytesWritten = 0;
                                int bytesToWrite = 1024;
                                int totalBytes = printData.length;
                                int remainingBytes = totalBytes;
                                while (bytesWritten < totalBytes) {
                                    if (remainingBytes < bytesToWrite)
                                        bytesToWrite = remainingBytes;
                                    //Send data, 1024 bytes at a time until all data sent
                                    conn.write(printData, bytesWritten, bytesToWrite);
                                    bytesWritten += bytesToWrite;
                                    remainingBytes = remainingBytes - bytesToWrite;
                                    Thread.sleep(100);
                                }

                                if (cantidaddecopias == 1) {
                                    Thread.sleep(5000);
                                    bytesWritten = 0;
                                    bytesToWrite = 1024;
                                    totalBytes = printData.length;
                                    remainingBytes = totalBytes;
                                    while (bytesWritten < totalBytes) {
                                        if (remainingBytes < bytesToWrite)
                                            bytesToWrite = remainingBytes;
                                        conn.write(printData, bytesWritten, bytesToWrite);
                                        bytesWritten += bytesToWrite;
                                        remainingBytes = remainingBytes - bytesToWrite;
                                        Thread.sleep(100);
                                    }
                                }

                                DisplayPrintingStatusMessage("Impresión Exitosa. ");

                                contador = 0;
                                conn.close();

                                EnableDialog(false, "cerrar", false);
                                if (minimizarapp == 2) {
                                    moveTaskToBack(true);
                                }
                            }
                        }

                    } catch (Exception e) {
                        if (conn != null) {
                            conn.close();
                        }
                        e.printStackTrace();
                        EnableDialog(false, "", false);
                    }
            }
        }).start();



    }


    private String message;

    @Override
    public void run() {

        if (bandera) {
            try {
                if (contador > 0) {
                    EnableDialog(true, "Enviando Documento... reintentando", false);
                } else {
                    EnableDialog(true, "Enviando Documento...", false);
                }
                contador++;
                conn = null;

                if (connectionType.equals("Bluetooth")) {
                    Looper.prepare();
                    conn = Connection_Bluetooth.createClient(m_printerMAC, false);
                }
                if (!conn.getIsOpen()) {
                    if (conn.open()) {

                        int bytesWritten = 0;
                        int bytesToWrite = 1024;
                        int totalBytes = printData.length;
                        int remainingBytes = totalBytes;
                        while (bytesWritten < totalBytes) {
                            if (remainingBytes < bytesToWrite)
                                bytesToWrite = remainingBytes;
                            //Send data, 1024 bytes at a time until all data sent
                            conn.write(printData, bytesWritten, bytesToWrite);
                            bytesWritten += bytesToWrite;
                            remainingBytes = remainingBytes - bytesToWrite;
                            Thread.sleep(100);
                        }

                        if (cantidaddecopias == 1) {
                            Thread.sleep(5000);
                            bytesWritten = 0;
                            bytesToWrite = 1024;
                            totalBytes = printData.length;
                            remainingBytes = totalBytes;
                            while (bytesWritten < totalBytes) {
                                if (remainingBytes < bytesToWrite)
                                    bytesToWrite = remainingBytes;
                                conn.write(printData, bytesWritten, bytesToWrite);
                                bytesWritten += bytesToWrite;
                                remainingBytes = remainingBytes - bytesToWrite;
                                Thread.sleep(100);
                            }
                        }


                        DisplayPrintingStatusMessage("Impresión Exitosa. ");

                        contador = 0;
                        conn.close();
                        bandera = false;
                        EnableDialog(false, "cerrar", false);
                        if (minimizarapp == 2) {
                            moveTaskToBack(true);
                        }
                    }
                }

            } catch (Exception e) {
                //signals to close connection
                if (conn != null) {
                    conn.close();
                }
                e.printStackTrace();
                EnableDialog(false, "", false);

                if (contador <= 1) {
                    try {
                        if (bandera) {
                            new Thread(Pdf_view.this, "PrintingTask").start();
                        } else {
                            contador = 0;
                            if (conn != null) {
                                conn.close();
                            }
                        }
                    } catch (Exception el) {
                        el.printStackTrace();
                    }
                } else {
                    contador = 0;
                    EnableDialog(false, "cerrar", false);
                }
            }
        } else {
            contador = 0;
            bandera = true;
            if (conn != null) {
                conn.close();
            }
        }
    }





}