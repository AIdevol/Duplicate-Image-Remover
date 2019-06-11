# Duplicate Image Remover #
This program is designed to search folders for duplicate images and notify the user when a potential duplicate 
has been found. The user will then be able to see details about each image, as well as the images themselves, 
and choose which one to remove, if either.

## Features ##
* When a potential match has been found...
    * The program displays information about each potential duplicate, including the name of each image, the 
    image paths, the image dimensions, the amount of storage space used by each image, and the file extensions.
    * The program shows where differences between the two matching images are found, both by highlighting pixels 
    that are not exactly the same (can be seen on the "Pixel Differences" tab) and by subtracting the images 
    from each other (can be seen on the "Subtracted Differences" tab).
    * Displays how similar the two images are in a percentage.

* The program can compare images of different sizes with each other, but only as long as they both have the same
height/width ratio.

* Has an option to notify the user when a potential match is found with a beeping noise, and another option to
periodically repeat this notification sound if the user is AFK and wishes to hear it when they return. This can
be helpful when the user wishes to have other tabs open while the program is running.

## Limitations ##
* The program cannot match an image with a cropped, rotated, or otherwise highly edited version of itself.
Most-likely, the program will think these are two unique images and will not mark them as potential duplicates.

* If you are working with a lot of images that have the same background color, the program may come up with
a lot of false positives and display images that are not supposed to be paired. This is because the 
majority of the image has the same color in it, which results in a higher percentage of similarity. The best
solution to this is to increase the percent similarity threshold, which should reduce the number of false
positives.

* The user cannot set the percent similarity threshold to 100% if they want the program to compare images of
different sizes. The reason for this is that the program must resize these images (not the originals, just 
a copy of them) so that they have the same height and width before it can compare them pixel-by-pixel.
Unfortunately, the program cannot scale these images without distorting the image slightly. Most of the time
these distortions are too small to notice, but they do reduce the percentage of similarity between the images so
that they are below 100%, which is why the duplicate does not get shown to the user at a 100% similarity threshold.

* The image in the Pixel Differences tab on the right does not accurately show pixel differences when the program
is comparing two images of different sizes. This is because of the previously mentioned resizing issue, where 
resizing the image slightly changes the copy the program is comparing. This is enough to make the pixels between 
the two images unique, resulting in the mostly-white image the user sees. If this happens, just use the image 
generated on the Subtracted Differences tab instead, since it is less effected by this issue.

* When choosing to delete a file, the program cannot send the file directly to the Recycle Bin. Any images deleted
using this program will skip the Recycle Bin and be deleted *permenantly*!

## Requirements ##
...
