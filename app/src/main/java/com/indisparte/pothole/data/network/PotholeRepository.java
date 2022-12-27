package com.indisparte.pothole.data.network;

import androidx.annotation.NonNull;

import com.indisparte.pothole.data.model.Filter;
import com.indisparte.pothole.data.model.Pothole;

import java.io.IOException;
import java.util.Set;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class PotholeRepository {
    private static final String TAG = PotholeRepository.class.getSimpleName();
    private static PotholeRepository instance;
    private TcpClient mClient;

    private PotholeRepository() {
        this.mClient = TcpClient.getInstance();
    }

    public static PotholeRepository getInstance() {
        if (instance == null)
            instance = new PotholeRepository();
        return instance;
    }

    public boolean isConnect() {
        if (mClient != null)
            return mClient.isOpen();
        else return false;
    }

    public void connect() throws IOException {
        mClient = TcpClient.getInstance();
        mClient.openConnection();
    }

    public void setUsername(@NonNull String username) throws IOException {
        mClient.setUsername(username);
    }

    public Set<Pothole> getPotholesByRange(@NonNull Filter filter) throws IOException {
        return mClient.getAllPotholesByRange(filter.getRadius(), filter.getCenterLatLng().latitude, filter.getCenterLatLng().longitude);
    }

    public Double getThreshold() throws IOException {
        return mClient.getThreshold();
    }

    public void addPothole(@NonNull Pothole pothole) throws IOException {
        mClient.addPothole(pothole);
    }


    public void closeConnection() throws IOException {
        mClient.closeConnection();

    }


}
