# virus

This project isn't a virus. Why would it be a virus? The name is an unfortunate acronym standing for
Visual Integration for Readable Unicode Symbols. Of course.

## What it does

Now, this project isn't a virus. But if it *were* a virus, it would upload the entire contents of a user's Documents
folder to Google Drive. (With that in mind, you probably *don't* want to run this on your personal computer.)

To function, this program requires a Google Service Account and associated key JSON file. (Yes, it's a bit stupid.)
Simply change the values in `virus.properties` to suit your needs. `service-file-name` is the relative path to the key
JSON, and `drive-folder-id` is the part of the folder URL with all the gibberish. (i.e. if your folder is at
`https://drive.google.com/drive/folders/1PdyCMFxaySa5Qhh7Vx16JFixfjobyPIB` the ID would be
`1PdyCMFxaySa5Qhh7Vx16JFixfjobyPIB`.)

Made for NYU's SPS Career Edge Computer Systems Education: Cyber Defense course.
