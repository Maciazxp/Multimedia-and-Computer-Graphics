
## Java Video Creator - README
**Project Description**
This is a console-based Java application that takes a set of photos and short videos and automatically produces a narrated portrait-mode video. The program handles everything: it analyzes the visual content of your files, generates an AI cover image, builds a closing map slide from the GPS coordinates, writes and voices a narration script, and assembles the final video

It uses the OpenAI API (GPT-4o, DALL-E 3, TTS-1), FFmpeg for video assembly, ExifTool for metadata extraction, and OpenStreetMap for the map slide.

---

# Main Features
1. **Automatic Metadata Extraction**
   The program reads GPS coordinates and capture dates directly from each file's EXIF data using ExifTool. No manual input required.

2. **Chronological Sorting**
   Files are automatically sorted from oldest to newest based on their capture date before the program start to analize the videos.

3. **AI Cover Image (DALL-E 3)**
   The program sends your images to GPT-4o Vision to analyze what's actually visible in them, then uses that description to generate image with DALL-E 3.

4. **Narration Script + Voice (GPT-4o + TTS-1)**
   GPT-4o Vision writes a short narration for each image based only on what is literally visible — no invented backstories. The script is then converted to audio using OpenAI's TTS-1 model with the "alloy" voice.

5. **Closing Map Slide**
   A map image is assembled from OpenStreetMap tiles showing a route between the oldest and newest file locations. Two pins mark the start (green) and end (red) of the journey. An AI-generated inspirational phrase is overlaid at the bottom.

6. **Portrait Video Output**
   All slides and clips are combined into a single 1080x1920 portrait video with the narration audio. Photos and videos with any aspect ratio are handled using a blurred background to avoid black bars.

---

## Getting Started
**Prerequisites**

- [FFmpeg](https://ffmpeg.org/) installed and available in your PATH
- [ExifTool](https://exiftool.org/) installed and available in your PATH
- An OpenAI API key with access to GPT-4o, DALL-E 3, and TTS-1

**Setting up the API Key**

The program reads the OpenAI key from an environment variable. Set it before running, it is essential

**Execution Steps**

Compile the Java files:

-*Bash*

```
javac *.java
```

Run the program:

-*Bash*

```
java Main
```

**Typical Workflow**
1. The program asks how many files you want to process.
2. Enter the path to each file one by one (photos or short videos, max ~5 seconds each please :( )).
3. Wait while the program runs through all 7 steps automatically.
4. Optionally verify the audio loudness levels at the end.
5. The final video is saved as `final_video_portrait.mp4` in the working directory (outside the source code or src root).

---

## Files Generated at Runtime
| File | Description |
|------|-------------|
| `AiImage.png` | AI-generated cover image (DALL-E 3) |
| `audio.mp3` | Narration audio (TTS-1) |
| `mapImage.png` | Closing map slide with GPS pins and phrase |
| `final_video.mp4` | The horizontal video |
| `final_video_portrait.mp4` | Final portrait output |
| `videopart_*.mp4` | Temporary parts created during the process |
| `input.txt` | Temporary FFmpeg concat list |

---

## Important Considerations
**File Requirements**: All files should have EXIF metadata embedded (GPS coordinates and capture date). The program handles missing metadata gracefully, but the map slide and narration will be less accurate without it.

**Video Length**: Input videos should be no longer than ~5 seconds. Longer videos will still work but may produce a poorly paced final result since each clip plays at its original length.

**Supported Formats**: Photos (JPG, PNG) and videos (MP4, MOV, AVI, MKV, WEBM, M4V). Output is always MP4.

**Audio Timing**: The narration covers the full video. Each photo slide is shown for 5 seconds, so if the narration for a particular image runs longer it will overlap with the next slide.

**Map Accuracy**: The map is centered between the oldest and newest GPS coordinates. The zoom level is chosen automatically based on how far apart the two locations are — from street level up to continental scale.

**No GPS Data**: If none of your files have GPS coordinates, the map slide will be replaced by a plain dark image with the inspirational phrase still overlaid.

**Internet Required**: The program needs an active connection to reach the OpenAI API and the OpenStreetMap tile server during execution.
