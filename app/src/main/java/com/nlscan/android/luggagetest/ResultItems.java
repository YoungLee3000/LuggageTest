package com.nlscan.android.luggagetest;

class ResultItems {

    private String epcId;
    private String boxId;
    private String hugState;
    private String layState;
    private String carId;

    public ResultItems(){

    }

    public ResultItems(String epcId, String boxId, String hugState, String layState, String carId) {
        this.epcId = epcId;
        this.boxId = boxId;
        this.hugState = hugState;
        this.layState = layState;
        this.carId = carId;
    }

    public String getEpcId() {
        return epcId;
    }

    public void setEpcId(String epcId) {
        this.epcId = epcId;
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getHugState() {
        return hugState;
    }

    public void setHugState(String hugState) {
        this.hugState = hugState;
    }

    public String getLayState() {
        return layState;
    }

    public void setLayState(String layState) {
        this.layState = layState;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }
}
