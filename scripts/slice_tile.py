############################## SIMPLE TILE SLICER SCRIPT #############################
# NOTE:                                                                              #
#   This tool takes one input texture, asks for the pixel size, then asks for a      #
# destination directory. The textures will then be sliced.                           #
#                                                                                    #
# Tool copyright 2024 RSC Games. All rights reserved.                                #
######################################################################################

import PIL
import os
import sys
import hashlib
import tkinter as tk
from tkinter import simpledialog
from tkinter import filedialog

import PIL.Image


def get_output_folder():
    """
    Get a file path to the output folder.

    :return str: The path to the output folder.
    """

    print("Please select an output folder...")
    root = tk.Tk()
    root.withdraw()
    folder_path = filedialog.askdirectory(
        initialdir=os.getcwd(),  # Default path.
        mustexist=True,
        title="Select output folder..."  # Title.
    )
    return folder_path


def get_input_image():
    """
    Get a file path to an image to slice.

    :return str: The path to the image.
    """

    print("Please select an image to slice...")
    root = tk.Tk()
    root.withdraw()
    file_path = filedialog.askopenfilename(
        filetypes=[("Image Files (.png, .jpg, .bmp)", [".png", ".jpg", ".bmp"])], # File types
        initialdir=os.getcwd(),  # Default path.
        title="Select image file to slice..."  # Title.
    )
    return file_path


def get_tile_stride():
    """
    Ask the user for the tile stride.
    
    :return int: Tile stride
    """
    val = simpledialog.askinteger(
        "Enter tile stride",
        "Please enter the tile size (in pixels).",
        minvalue=1
    )

    print(f"Got tile stride: {val} px")
    return val


def slice_image(in_file, stride):
    img = PIL.Image.open(in_file)
    print(img.has_transparency_data)

    if img.width % stride != 0 or img.height % stride != 0:
        print("Warning! Input image cannot be fully broken into tiles!")

    tileID = 0
    for y in range(0, img.height, stride):
        for x in range(0, img.width, stride):
            cropped = img.crop((x, y, x+stride, y+stride))
            #print(f"saving file {tileID} at coords {x}, {y}")
            #print(cropped.width, cropped.height)

            # Trim out images with no data.
            if (cropped.getcolors()[0] == (stride ** 2, (0, 0, 0, 0))):
                #print(f"skipping zero alpha image {cropped.getcolors()}")
                continue

            cropped.save(f"tile_{tileID}.png", format="PNG")
            cropped.close()
            tileID += 1

    print(f"Sliced {tileID} tiles.")
    return tileID  # Total saved tiles


def remove_duplicates(prefix, tile_count):
    """
    Remove all duplicate tiles within a tilemap.
    """

    print(f"Trimming tiles in path {os.getcwd()}")
    hash_list = []

    # Technically listdir but this can be fixed later.
    for id in range(0, tile_count):
        file = f"{prefix}{id}.png"

        f = open(file, "rb")
        hash = hashlib.file_digest(f, "sha512")
        hash_str = hash.hexdigest()
        file_size = os.path.getsize(file)
        index = (hash_str, file_size)
        f.close()

        # If the hash is present, mark this file for deletion; otherwise add the hash.
        if index in hash_list:
            print(f"Detected hash collision on file {file} hash {index}")
            os.unlink(file)
        else:
            hash_list.append(index)
            

def main():
    print("Tile set slicer script. Copyright 2024 RSC Games. All rights reserved.")
    in_file_path = get_input_image()
    
    if in_file_path == "":
        print("No file selected to slice!")
        sys.exit(1)

    out_folder_path = get_output_folder()

    if out_folder_path == "":
        print("No output folder selected!")
        sys.exit(2)

    os.chdir(out_folder_path)
    tile_stride = get_tile_stride()
    print(os.getcwd())
    
    saved_tiles = slice_image(in_file_path, tile_stride)
    remove_duplicates("tile_", saved_tiles)

if __name__ == "__main__":
    main()