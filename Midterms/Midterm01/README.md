
## Image Processing Tool - README
**Project Description**
This is an interactive image processing tool developed in Java that allows users to select a rectangular region of an image and apply various operations to it. The program utilizes barycentric coordinates to work with quadrilateral regions and features an interactive menu for applying multiple operations sequentially.

# Main Features
1. Custom Region Selection 
Users can define any rectangular area of the image by specifying:
Top-left X coordinate 
Top-left Y coordinate 
Rectangle width 
Rectangle height 
The region can be redefined at any time during the session.

2. Processing Operations

        Crop: Extracts the content of the selected region and creates a new image. Subsequent operations are applied to this new image.

        Negative Colors: Inverts the colors within the selected region using the formula: new_value=255−original_value for each RGB component. This creates a photographic negative effect without changing the image size.

        Rotate: Rotates the content of the selected region within the image. Supported angles include 90°, 180°, and 270°. The rotation is centered on the region, and the original area is cleared to white before placing the rotated content.

## Getting Started
**Prerequisites**
Java Development Kit (JDK) 8 or higher.
An image in JPG or PNG format (default path: src/i.jpg).

**Execution Steps**
Compile the Java files:

-*Bash*

javac *.java
Run the program:

-*Bash*

## java Main
**Typical Workflow**

        Define the working region: Enter X, Y, width, and height.

        Select an operation: Choose from Crop, Negative Colors, Rotate, Define new region, or Finish.

        Save the result: Provide a filename for the final PNG image.

## Operating Logic
**Barycentric Coordinates**
The program uses barycentric coordinates to determine if a pixel is within the selected region. The region is divided into two triangles:

Triangle 1: A-B-C 

Triangle 2: A-C-D 

A point is considered inside if its barycentric coordinates are all ≥0 in either triangle. Once the pixel is confirmed to be within the area, the modifications are applied.

## Important Considerations
**Bounds Validation**: The program automatically adjusts limits if the user enters coordinates outside the image range.

**Post-Crop Behavior**: After a Crop operation, the working image changes size and the region is automatically redefined to cover the entire new image.

**Image Formats**: Input can be JPG or PNG, but the program always saves the output in PNG format.

**Rotation**: Only 90°, 180°, and 270° angles are supported. The original area is painted white before the rotated content is placed.