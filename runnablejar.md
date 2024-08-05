Here is the revised text with proper grammar and formatting for a Markdown file:


The jar file is built using OpenJDK version "21.0.1" (2023-10-17).

To run the jar file:

1. Get the code from the repository:
    ```sh
    git clone https://github.com/aprdbapp/dcafixer.git
    ```
2. Please download this jar file [HERE](https://drive.google.com/file/d/1VnXIfMKngVhgro62IMoHBopYa2MvZsbM/view?usp=sharing).
3. Move the jar file to `path/to/dcafixer/build`.
4. Navigate to the `dcafixer` directory:
    ```sh
    cd /path/to/dcafixer
    ```
5. Run the bash script `run-examples.sh`. The script passes the path of the files to the test cases. You can edit it to point to other files if you want:
    ```sh
    sh run-examples.sh
    ```

You should see the output described in the `README.md` repository. If you want to repeat the experiment, remove the files by running `clean-examples.sh`:
    ```sh
    sh clean-examples.sh
    sh run-examples.sh
    ```
