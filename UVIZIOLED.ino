/*
 * Usage: Send 2-digit HEX array to the RFduino. (Array of double-digit hex values).
 * 
 * Array Definiton (Corresponds to the Data struct definiton below)
 * 
 * 0: mode value  -> See MODE_X options below
 * 1: period      -> time (in ms) between iterations. Rounded up to MIN_PERIOD if below. Currently doesn't do much since the max for a 2-digit hex (255) is < 500...
 * 2: num_pixels  -> number of pixels/frames to iterate through. Doesn't matter unless you're using MODE_FRAMES. Assumed <= MAX_FRAMES.
 * 3-5+           -> color intensity value. 3-> r (0-255 or FF), 4 -> g, 5 -> b. If num_pixels = 2, then 6-> r2, 7->g, 8->b for frame 2, etc. 
 * 
 */



#include <RFduinoBLE.h>

#define MODE_STATIC 0   // Display a static color.
#define MODE_BLINK  1   // Blink a color on/off at the specified period (ms)
#define MODE_FADE   2   // Like blink, but fade instead. Not yet working. MODE_FRAMES can handle this by defining fade "between" frames.
#define MODE_FRAMES 3   // Ability to define frames of pixels to loop between. 
                        // Loops the whole pattern by the period, and frames are equally divided between the period's time.

#define MIN_PERIOD 500
#define MAX_FRAMES 10

// pin 2 on the RGB shield is the red led
int RED = 2;
// pin 3 on the RGB shield is the green led
int GREEN = 3;
// pin 4 on the RGB shield is the blue led
int BLUE = 4;




typedef struct Pixel {
  uint8_t r;
  uint8_t g;
  uint8_t b;
  
} Pixel;

typedef struct Data {
  uint8_t mode;
  int period;
  int num_pixels;
  Pixel pixels[MAX_FRAMES];
} Data;


Pixel PIXEL_RED   = {255, 0, 0};
Pixel PIXEL_GREEN = {0, 255, 0};
Pixel PIXEL_BLUE  = {0, 0, 255}; 
Pixel PIXEL_RG    = {255, 255, 0};
Pixel PIXEL_GB    = {0, 255, 255};
Pixel PIXEL_RB    = {255, 0, 255};
Pixel PIXEL_WHITE = {255, 255, 255};
Pixel PIXEL_BLACK = {0, 0, 0};
Pixel PIXEL_OFF = PIXEL_BLACK;
Pixel PIXEL_ON  = PIXEL_WHITE;

Data DATA = {MODE_STATIC, MIN_PERIOD, 1, {PIXEL_OFF} };
char *DEFAULT_RECIEVE = {MODE_STATIC, MIN_PERIOD, 1, {PIXEL_OFF}};

void setup() {
  // setup the leds for output
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);  
  pinMode(BLUE, OUTPUT);

  Serial.begin(9600);

  //analogWrite(led1, 255);
  //analogWrite(led2, 255);
  //analogWrite(led3, 255);
  

  // this is the data we want to appear in the advertisement
  // (if the deviceName and advertisementData are too long to fix into the 31 byte
  // ble advertisement packet, then the advertisementData is truncated first down to
  // a single byte, then it will truncate the deviceName)
  RFduinoBLE.advertisementData = "rgb";
  
  // start the BLE stack
  RFduinoBLE.begin();
}

void displayStatic(Pixel data)
{
  analogWrite(RED, data.r);
  analogWrite(GREEN, data.g);
  analogWrite(BLUE, data.b);
}

void displayBlink(Data data) 
{
    Pixel p = data.pixels[0];
    displayStatic(p);
    delay(data.period/2);
    displayStatic(PIXEL_OFF);
    delay(data.period/2);
}

void displayFrames(Data data) {

    int wait = data.period / data.num_pixels;
    
    for(int i = 0; i < data.num_pixels; i++) 
    {
      Pixel p = data.pixels[i];
      displayStatic(p);
      delay(wait);
    }
}

void loop() {
  // switch to lower power mode
  // RFduino_ULPDelay(INFINITE);

  // delay(DATA.period);
  /*
  Serial.print("displaying: ");
  //Serial.print(DATA.mode);
  //Serial.print(": ");
  Serial.print(DATA.pixels[0].r);
  Serial.print(", ");
  Serial.print(DATA.pixels[0].g);
  Serial.print(", ");
  Serial.println(DATA.pixels[0].b);
  */

  switch(DATA.mode) {
    case MODE_STATIC : displayStatic(DATA.pixels[0]); break;
    case MODE_BLINK  : displayBlink(DATA); break;
    case MODE_FRAMES : displayFrames(DATA); break;
  }



  
}

void runTest() {
  DATA.mode = MODE_STATIC;
  DATA.period = MIN_PERIOD;
  DATA.num_pixels = 1;
  DATA.pixels[0] = PIXEL_OFF; // prevent loop from overriding

  Serial.println("Testing Individual colors & combinations...");
  displayStatic(PIXEL_RED);
  delay(MIN_PERIOD);
  displayStatic(PIXEL_GREEN);
  delay(MIN_PERIOD);
  displayStatic(PIXEL_BLUE);
  delay(MIN_PERIOD);
  displayStatic(PIXEL_RG);
  delay(MIN_PERIOD);
  displayStatic(PIXEL_GB);
  delay(MIN_PERIOD);
  displayStatic(PIXEL_RB);
  delay(MIN_PERIOD);
  displayStatic(PIXEL_WHITE);
  delay(MIN_PERIOD);
  displayStatic(PIXEL_BLACK);
  delay(MIN_PERIOD);

  Serial.println("Testing BLINK mode at 3 speeds...");

  int multiplier = 1;
  DATA.pixels[0] = PIXEL_RED;
  DATA.period = MIN_PERIOD/multiplier;
  for(int i = 0; i < (5*multiplier); i++)
  {
    displayBlink(DATA);
  }

  multiplier = 2;
  DATA.pixels[0] = PIXEL_GREEN;
  DATA.period = MIN_PERIOD/multiplier;  
  for(int i = 0; i < (5*multiplier); i++)
  {
    displayBlink(DATA);
  }

  multiplier = 4;
  DATA.pixels[0] = PIXEL_BLUE;
  DATA.period = MIN_PERIOD/multiplier;
  for(int i = 0; i < (5*multiplier); i++)
  {
    displayBlink(DATA);
  }

  Serial.println("Testing RED-BLUE frame loop...");
  DATA.period = MIN_PERIOD;
  DATA.num_pixels = 2;
  DATA.pixels[0] = PIXEL_RED;
  DATA.pixels[1] = PIXEL_BLUE;
  for(int i = 0; i < 10; i++)
  {
    
    displayFrames(DATA);
    // don't delay here!! delay(MIN_PERIOD);
  }

  Serial.println("Tests complete.");
  DATA.mode = MODE_STATIC;
  DATA.num_pixels = 1;
  DATA.pixels[0] = PIXEL_OFF;
}

void RFduinoBLE_onConnect() {
  // the default starting color on the iPhone is white
  //analogWrite(led1, 255);
  //analogWrite(led2, 255);
 //analogWrite(led3, 255);

  DATA = {MODE_STATIC, MIN_PERIOD, 1, {PIXEL_WHITE}};

  runTest();

  Serial.println("CONNECTED. SETTING {255, 255, 255}");
}

void RFduinoBLE_onDisconnect() {
  // turn all leds off on disconnect and stop pwm
 // digitalWrite(led1, LOW);
 // digitalWrite(led2, LOW);
 // digitalWrite(led3, LOW);

  DATA = {MODE_STATIC, MIN_PERIOD, 1, {PIXEL_OFF}};
}


void RFduinoBLE_onReceive(char *data, int len) {
  // each transmission should contain an RGB triple
  if (len < 3) data = DEFAULT_RECIEVE;
  {
    

    // get the speed values
    DATA.mode = data[0];
    DATA.period = (data[1] < MIN_PERIOD ? MIN_PERIOD : data[1]);
    DATA.num_pixels = (data[2] < 1 ? 1 : data[2]);

    //Pixel pixels[MAX_FRAMES];

    int base = 3;
    for(int i=0; i<DATA.num_pixels; i++)
    {
      // get the RGB values

      int color_start = base + (i*3);
      
      uint8_t r = data[color_start];
      uint8_t g = data[color_start+1];
      uint8_t b = data[color_start+2];

      DATA.pixels[i] = {r,g,b};

    }
    /*
    DATA.mode = mode;
    DATA.period = period;
    DATA.num_pixels = num_pixels;
    //DATA.pixels = pixels;
   */
  }
}
