form: "Enter parameters"
   sentence: "Audio filename", "x"
   sentence: "Text out filename", "x"
endform



writeFileLine: text_out_filename$, "time,pitch"
audio = Read from file: audio_filename$
select 'audio'

To Pitch: 0, 75, 600
no_of_frames = Get number of frames

for frame from 1 to no_of_frames
    time = Get time from frame number: frame
    pitch = Get value in frame: frame, "Hertz"
    appendFileLine: text_out_filename$, "'time','pitch'"
endfor