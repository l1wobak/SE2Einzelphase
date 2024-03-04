package com.example.se2einzelphase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.se2einzelphase.databinding.FragmentFirstBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EditText inputField;
    private TextView resultTextView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        View view = binding.getRoot();
        inputField = view.findViewById(R.id.editTextMatNo);
        resultTextView = view.findViewById(R.id.textview_server_response);
        return view;




    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String matriculationNumber = inputField.getText().toString();

                resultTextView.setText("");
                calculateMatriculationNumber(matriculationNumber);
                sendMatriculationNumber(matriculationNumber);

            }


            }
        );
    }

    private void calculateMatriculationNumber(String matriculationNumber) {

        /**
         * //Matrikelnummer 12203495 = Aufgabe 3
         *Matrikelnummer sortieren, wobei zuerst alle geraden,
         * dann alle ungeraden Ziffern gereiht sind (erst die
         * geraden, dann alle ungeraden Ziffern aufsteigend
         * sortiert)
         */

        int matno = Integer.parseInt(matriculationNumber);
        List<Integer> evenDigits = new ArrayList<>();
        List<Integer> oddDigits = new ArrayList<>();

        while (matno > 0) {
            int digit = matno % 10;

            if (digit % 2 == 0) {
                evenDigits.add(digit);
            } else {
                oddDigits.add(digit);
            }
            matno /= 10;
        }

        Collections.sort(evenDigits);
        Collections.sort(oddDigits);
        evenDigits.addAll(oddDigits);


        resultTextView.append("Rechnung mit Matrikelnummer: "+ evenDigits+"\n");

    }

    private void sendMatriculationNumber(final String matriculationNumber) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("se2-submission.aau.at", 20080);

                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(matriculationNumber);

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        responseBuilder.append(line);
                    }

                    final String result = responseBuilder.toString();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTextView.append("Serverantwort: " + result+"\n");
                        }
                    });

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}