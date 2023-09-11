#include <iostream>
#include <string>
#include <vector>
#include <stdlib.h>
#include <cmath>
#include <random>
#include <time.h>

using namespace std;

// colors
#define RESET "\e[0m"
#define RED "\e[1;31m"
#define GREEN "\e[1;32m"
#define YELLOW "\e[1;33m"
#define BLUE "\e[1;34m"

#define toggle(x) (x == '0' ? '1' : '0')

string asciiToBinary(char c)
{
    string binary = "";
    int asciiValue = (int)c;
    for (int i = 0; i < 8; i++)
    {
        binary = (char)(asciiValue % 2 + 48) + binary;
        asciiValue /= 2;
    }
    return binary;
}

string binaryToAscii(string s)
{
    string ascii = "";
    int asciiValue = 0;
    for (int i = 0; i < s.size(); i++)
    {
        asciiValue = asciiValue * 2 + (int)s[i] - 48;
        if (i % 8 == 7)
        {
            ascii += (char)asciiValue;
            asciiValue = 0;
        }
    }
    return ascii;
}

string CRC(string data, string generator, bool isSender = true)
{

    if (isSender)
    {
        for (int i = 0; i < generator.size() - 1; i++)
            data += "0";
    }

    int dataLength = data.size();
    int generatorLength = generator.size();

    for (int i = 0; i + generatorLength - 1 < dataLength; i++)
    {

        if (data[i] == '0')
            continue;

        int found = -1;
        for (int k = 0; k < generatorLength; k++)
        {

            data[i + k] = data[i + k] == generator[k] ? '0' : '1';
            if (data[i + k] == '1' && found != -1)
                found = i + k;
        }

        if (found == -1)
            continue;
        if (found < dataLength - generatorLength)
        {
            i = found - 1;
            continue;
        }
    }

    string remainder = data.substr(dataLength - generatorLength + 1, generatorLength - 1);
    return remainder;
}

string calculateCheckBits(string databits)
{
    string checkBits = "";
    int checkBitIndex = 1;
    int dataIndex = 0;

    for (int i = 0; dataIndex < databits.size(); i++)
    {
        if (checkBitIndex == i + 1)
        {
            checkBits += "0";
            checkBitIndex *= 2;
            continue;
        }

        if (databits[dataIndex++] == '0')
            continue;

        for (int j = 0; j < checkBits.size(); j++)
        {
            if ((i + 1) & (1 << j))
                checkBits[j] = checkBits[j] == '0' ? '1' : '0';
        }
    }
    return checkBits;
}

int main()
{
    srand(time(NULL));

    string dataString, generatorPolynomial;
    int m;
    double p;

    // ======================== Input ========================
    cout << "enter data string: ";
    getline(cin, dataString);

    cout << "enter number of data bytes in a row (m): ";
    cin >> m;

    cout << "enter probability (p): ";
    cin >> p;

    cout << "enter generator polynomial: ";
    cin >> generatorPolynomial;
    cout << endl;

    // ======================== Padding ========================
    int dataStringSize = dataString.size();
    int paddingSize = (m - (dataString.size() % m)) % m;
    for (int i = 0; i < paddingSize; i++)
        dataString += "~";
    cout << "data string after padding: " << dataString << endl;
    cout << endl;

    // ======================== Converting to binary ========================
    vector<string> dataBlocks;
    string row = "";
    for (int i = 0; i < dataString.size(); i++)
    {
        row += asciiToBinary(dataString[i]);
        if (i % m == m - 1)
        {
            dataBlocks.push_back(row);
            row = "";
        }
    }

    cout << "data blocks (ascii code of m characters per row):" << endl;
    for (int i = 0; i < dataBlocks.size(); i++)
        cout << dataBlocks[i] << endl;
    cout << endl;
    cout << endl;

    // ======================== Adding check bits ========================
    vector<string> modifiedDataBlocks;

    for (string s : dataBlocks)
    {
        string checkBits = calculateCheckBits(s);
        string modifiedDataBlock = "";
        int checkBitIndex = 1;
        int checkBitCount = 0;

        for (int i = 0; i < s.size(); i++)
        {
            while (modifiedDataBlock.size() == checkBitIndex - 1)
            {
                modifiedDataBlock += checkBits[checkBitCount++];
                checkBitIndex *= 2;
            }
            modifiedDataBlock += s[i];
        }

        modifiedDataBlocks.push_back(modifiedDataBlock);
    }

    cout << "data block after adding check bits:" << endl;
    for (auto s : modifiedDataBlocks)
    {
        int checkBitIndex = 1;
        for (int i = 0; i < s.size(); i++)
        {
            if (checkBitIndex == i + 1)
            {
                cout << GREEN << s[i] << RESET;
                checkBitIndex *= 2;
                continue;
            }
            cout << s[i];
        }
        cout << endl;
    }
    cout << endl;

    // ======================== Column-wise serialization ========================
    string serializedDataBits = "";
    int length = modifiedDataBlocks[0].size();
    for (int i = 0; i < length; i++)
    {
        for (int j = 0; j < modifiedDataBlocks.size(); j++)
        {
            serializedDataBits += modifiedDataBlocks[j][i];
        }
    }

    cout << "data bits after column-wise serialization:" << endl;
    cout << serializedDataBits << endl;
    cout << endl;

    // ======================== CRC ========================
    string remainder = CRC(serializedDataBits, generatorPolynomial);
    cout << "data bits after appending CRC checksum (sent frame):" << endl;
    cout << serializedDataBits << BLUE << remainder << RESET << endl;
    cout << endl;

    // ======================== Error simulation ========================
    string receivedFrame = serializedDataBits + remainder;
    vector<int> errorIndices;
    // error simulation
    for (int i = 0; i < receivedFrame.size(); i++)
    {
        double random = (double)rand() / RAND_MAX;
        if (random < p)
        {
            receivedFrame[i] = receivedFrame[i] == '0' ? '1' : '0';
            errorIndices.push_back(i);
        }
    }

    cout << "received frame:" << endl;
    for (int i = 0, j = 0; i < receivedFrame.size(); i++)
    {
        if (j < errorIndices.size() && errorIndices[j] == i)
        {
            cout << RED << receivedFrame[i] << RESET;
            j++;
        }
        else
            cout << receivedFrame[i];
    }
    cout << endl;
    cout << endl;

    // ======================== CRC check ========================
    cout << "result of CRC checksum matching: ";
    remainder = CRC(receivedFrame, generatorPolynomial, false);
    if (remainder.find('1') != string::npos)
        cout << "error detected" << endl;
    else
        cout << "no error detected" << endl;
    cout << endl;

    // ======================== Removing CRC checksum ========================
    receivedFrame = receivedFrame.substr(0, receivedFrame.size() - generatorPolynomial.size() + 1);

    // ======================== deserialization ========================
    for (int i = 0, j = 0, k = 0; i < receivedFrame.size(); i++)
    {
        modifiedDataBlocks[j++][k] = receivedFrame[i];
        if (j == modifiedDataBlocks.size())
        {
            j = 0;
            k++;
        }
    }

    vector<vector<int>> errorIndicesPerBlock(modifiedDataBlocks.size());
    for (auto index : errorIndices)
    {
        int row = index % modifiedDataBlocks.size();
        int col = index / modifiedDataBlocks.size();
        errorIndicesPerBlock[row].push_back(col);
    }

    cout << "data block after removing CRC checksum bits:" << endl;
    for (int i = 0; i < modifiedDataBlocks.size(); i++)
    {
        for (int j = 0, k = 0; j < modifiedDataBlocks[i].size(); j++)
        {
            if (k < errorIndicesPerBlock[i].size() && errorIndicesPerBlock[i][k] == j)
            {
                cout << RED << modifiedDataBlocks[i][j] << RESET;
                k++;
            }
            else
                cout << modifiedDataBlocks[i][j];
        }
        cout << endl;
    }
    cout << endl;

    // ======================== Correct single error & remove check bits =================================
    for (int i = 0; i < modifiedDataBlocks.size(); i++)
    {
        int checkBitIndex = 1;
        for (int j = 0, k = 0; j < modifiedDataBlocks[i].size(); j++)
        {
            if (checkBitIndex == j + 1)
            {
                checkBitIndex *= 2;
                continue;
            }
            else
            {
                dataBlocks[i][k++] = modifiedDataBlocks[i][j];
            }
        }

        string newCheckBits = calculateCheckBits(dataBlocks[i]);
        checkBitIndex = 1;
        int checkBitCount = 0;
        int errorCount = 0;
        while (checkBitIndex < modifiedDataBlocks[i].size())
        {
            char oldCheckBit = modifiedDataBlocks[i][checkBitIndex - 1];
            char newCheckBit = newCheckBits[checkBitCount++];
            if (oldCheckBit != newCheckBit)
                errorCount += checkBitIndex;
            checkBitIndex *= 2;
        }
        if (errorCount != 0)
        {

            if ((errorCount & (errorCount - 1)) == 0)
                continue;
            int index = errorCount - log2(errorCount * 1.0) - 1;
            dataBlocks[i][index] = dataBlocks[i][index] == '0' ? '1' : '0';
        }
    }

    cout << "data block after removing check bits:" << endl;
    for (auto s : dataBlocks)
    {
        cout << s << endl;
    }
    cout << endl;


    // ======================== Converting to ascii ========================
    cout << "output frame: ";
    for (auto s : dataBlocks)
    {
        cout << binaryToAscii(s);
    }
    cout << endl;

    return 0;
}