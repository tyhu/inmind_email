import sys
import wave

fn = sys.argv[1]
pcmfile = open(fn, 'rb')
pcmdata = pcmfile.read()
wavfile = wave.open(fn+'.wav','wb')
wavfile.setparams((1, 2, 16000, 0, 'NONE', 'NONE'))
wavfile.writeframes(pcmdata)
