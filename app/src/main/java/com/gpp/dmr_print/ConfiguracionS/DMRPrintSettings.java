package com.gpp.dmr_print.ConfiguracionS;

import java.io.Serializable;

/**Class to save the application settings**/

public class DMRPrintSettings implements Serializable {

	private static final long serialVersionUID = 4762643389154364957L;

	//Set and get printers address
	private String printerIP;

	private String printerMAC;

	private int communicationMethod;

	private int selectedPrinterPort;

	private String selectedFolderPath;

	private String selectedFilePath;

	private int selectedItemIndex;

	private int selectedModeIndex;

	private int selectedAction;

	public int getSelecciondehojas() {
		return selecciondehojas;
	}

	public void setSelecciondehojas(int selecciondehojas) {
		this.selecciondehojas = selecciondehojas;
	}

	private int selecciondehojas;

	private int printheadWidth;//2018 PH - guardo el ancho de impresion seleccionado en texto

	private int printheadWidthIndex;//2018 PH - guardo el índice del ancho de impresión seleccionado

	private String selectedPrintMode;//2018 PH

	private String communicationType;//2018

	private int automatico;//2018

	private int densidad;

	private int timeout;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private String devicename;
	private String productoid;

	private Boolean estadoconfiguracion;

	public Boolean getEstadoconfiguracion() {
		return estadoconfiguracion;
	}

	public void setEstadoconfiguracion(Boolean estadoconfiguracion) {
		this.estadoconfiguracion = estadoconfiguracion;
	}

	public int getDensidad() {
		return densidad;
	}

	public void setDensidad(int densidad) {
		this.densidad = densidad;
	}




	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

	public String getProductoid() {
		return productoid;
	}

	public void setProductoid(String productoid) {
		this.productoid = productoid;
	}

	public int getAutomatico() {
		return automatico;
	}

	public void setAutomatico(int automatico) {
		this.automatico = automatico;
	}

	private int minimizar;
	public int getMinimizar() {
		return minimizar;
	}

	public void setMinimizar(int minimizar) {
		this.minimizar = minimizar;
	}

	//2018 PH - Accessors for printhead width index
	public int getPrintheadWidthIndex() {
		return printheadWidthIndex;
	}

	public void setPrintheadWidthIndex(int printheadWidthIndex) {
		this.printheadWidthIndex = printheadWidthIndex;
	}

	//2018 PH - Accessors for printhead width
	public int getPrintheadWidth() {
		return printheadWidth;
	}
	public void setPrintheadWidth(int printheadWidth) {
		this.printheadWidth = printheadWidth;
	}

	//2018 PH - Accessors for print mode
	public String getSelectedPrintMode() {
		return selectedPrintMode;
	}

	public void setSelectedPrintMode(String selectedPrintMode) {
		this.selectedPrintMode = selectedPrintMode;
	}

	//2018 PH - Accessors for communication mode
	public String getCommunicationType() {
		return communicationType;
	}

	public void setCommunicationType(String communicationType) {
		this.communicationType = communicationType;
	}

	//Accessors for selected action;
	public int getSelectedAction()
	{
		return selectedAction;
	}
	public void setSelectedAction(int value)
	{
		selectedAction = value;
	}

	//Accessors for selected item index;
	public int getSelectedItemIndex()
	{
		return selectedItemIndex;
	}
	public void setSelectedItemIndex(int value)
	{
		selectedItemIndex = value;
	}

	//Accessors for selected mode index;
	public int getSelectedModeIndex()
	{
		return selectedModeIndex;
	}
	public void setSelectedModeIndex(int value)
	{
		selectedModeIndex = value;
	}

	public String getPrinterIP() {
		return printerIP;
	}
	public void setPrinterIP(String value) {
		printerIP = value;
	}

	public String getPrinterMAC() {
		return printerMAC;
	}
	public void setPrinterMAC(String value) {
		printerMAC = value;
	}
	//Set and get printer's port

	public int getPrinterPort() {
		return selectedPrinterPort;
	}
	public void setPrinterPort(int value) {
		selectedPrinterPort = value;
	}
	//set and get communcation method
	public int getCommunicationMethod()
	{
		return communicationMethod;
	}
	public void setCommunicationMethod(int value)
	{
		communicationMethod = value;
	}

	//Constructor
	public DMRPrintSettings(String ip, String mac, int port, String folderPath, String filePath,int commMethod, int itemIndex, int modeIndex, int action, int action2, int mini, int auto, String device, String id,Boolean estado_configuracion,int densi, int timeout){
		printerIP = ip;
		printerMAC = mac;
		selectedPrinterPort  = port;
		selectedFolderPath = folderPath;
		selectedFilePath = filePath;
		communicationMethod= commMethod;
		selectedItemIndex = itemIndex;
		selectedModeIndex = modeIndex;
		selectedAction = action;
		selecciondehojas = action2;
		minimizar = mini;
		automatico = auto;
		devicename = device;
		productoid = id;
		estadoconfiguracion = estado_configuracion;
		densidad = densi;
		this.timeout = timeout;
	}


}