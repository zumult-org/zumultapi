# Praat Script: Draw Intonation Contour from Audio and TextGrid

# Set file paths
form Draw Intonation Contour
    sentence: "AudioFile", "X"
    sentence: "TextGridFile", "X"
    sentence: "OutputImage", "X"
endform

# Read the audio file
audio = Read from file... 'AudioFile$'

# Read the TextGrid file
textgrid = Read from file... 'TextGridFile$'

# Analyze the pitch
select 'audio'
select 'textgrid'
pitch = To Pitch... 0.0 75 600
select 'pitch'

# Open the Picture window
Erase all

# Draw the pitch contour
select 'pitch'
Draw... 0 0 75 600 1 1 "yes" # Draw pitch contour

# Highlight intervals in the selected tier
#Highlight intervals where... 'TierNumber' contains ""

Save as 600-dpi PNG file... 'OutputImage$'

printline Script completed successfully! Output saved to: 'OutputImage$'
