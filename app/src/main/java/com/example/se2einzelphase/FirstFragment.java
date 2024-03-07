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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EditText inputField;
    private TextView resultTextView;
    private static final String DOMAIN_NAME = "se2-submission.aau.at";
    private static final int PORT = 20080;
    private static final int MATRIKELNUMMER_MINIMUM_LENGTH = 7;
    private static final int MATRIKELNUMMER_MAXIMUM_LENGTH = 9;

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

                if(matriculationNumber.length()> MATRIKELNUMMER_MAXIMUM_LENGTH || matriculationNumber.length() < MATRIKELNUMMER_MINIMUM_LENGTH){
                    resultTextView.setText("Bitte geben Sie eine gültige Matrikelnummer ein (zwischen 7 und 9 Zeichen)");
                }
                else{
                    sendMatriculationNumber(matriculationNumber);
                }
            }
            }
        );

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String matriculationNumber = inputField.getText().toString();

                if(matriculationNumber.length()> MATRIKELNUMMER_MAXIMUM_LENGTH || matriculationNumber.length() < MATRIKELNUMMER_MINIMUM_LENGTH){
                    resultTextView.setText("Bitte geben Sie eine gültige Matrikelnummer ein (zwischen 7 und 9 Zeichen)");
                }
                else{
                    calculateMatriculationNumber(matriculationNumber);
                }
            }
        });
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

        final String result = "Rechnung mit Matrikelnummer"+evenDigits+"\n";

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String textfieldText = resultTextView.getText().toString();
                if(textfieldText.contains("\n")) {
                    resultTextView.setText(result+textfieldText.split("\n")[1]);
                }
                else {
                    resultTextView.setText(result+textfieldText);
                }

            }
        });


    }

    private void sendMatriculationNumber(final String matriculationNumber) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(DOMAIN_NAME, PORT);

                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(matriculationNumber);

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        responseBuilder.append(line);
                    }

                    String result = "Serverantwort: "+responseBuilder.toString();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String textfieldText = resultTextView.getText().toString();
                            if(textfieldText.contains("Serverantwort:")) {
                                resultTextView.setText(textfieldText.split("Serverantwort:")[0] + result);
                            }
                            else {
                                resultTextView.setText(textfieldText+result);
                            }

                        }
                    });

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTextView.setText("Error communicating with the Server");

                        }
                    });
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