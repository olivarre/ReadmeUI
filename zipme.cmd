@echo off

echo.
echo ================================================================================
echo (REO) Creates _readmeui.zip, _readmeui_with_src.zip, and a copy in (backups)
echo ================================================================================
pause

if not exist ".\bin" goto done
if not exist ".\readmeui.cmd" goto done

:: Use WMIC to retrieve date and time
FOR /F "skip=1 tokens=1-6" %%G IN ('WMIC Path Win32_LocalTime Get Day^,Hour^,Minute^,Month^,Second^,Year /Format:table') DO (
   IF "%%~L"=="" goto s_done
      Set _yyyy=%%L
      Set _mm=00%%J
      Set _dd=00%%G
      Set _sec=00%%K
      Set _hour=00%%H
      SET _minute=00%%I
)

:s_done
:: Pad digits with leading zeros
      Set _mm=%_mm:~-2%
      Set _dd=%_dd:~-2%
      Set _hour=%_hour:~-2%
      Set _minute=%_minute:~-2%
      Set _sec=%_sec:~-2%

:: Display the date/time in ISO 8601 format:
Set NOW3=%_yyyy%%_mm%%_dd%_%_hour%%_minute%pm

:zipme

echo.
echo ================================================================================
echo (REO) Creating _readmeui.zip
echo ================================================================================
del _readmeui.zip
7z u "_readmeui.zip" ".\*" -x!(backups) -x!*.zip -x!src -x!.classpath -x!.project -x!zipme.cmd

echo.
echo ================================================================================
echo (REO) Creating _readmeui_with_src.zip
echo ================================================================================
del _readmeui_with_src.zip
7z u "_readmeui_with_src.zip" ".\*" -x!(backups) -x!*.zip
goto results

:backup
echo.
echo ================================================================================
echo (REO) Copying _readmeui_with_src.zip to the (backups) directory...
echo ================================================================================
copy "_readmeui_with_src.zip" ".\(backups)\_readmeui_with_src_%NOW3%.zip"
dir ".\(backups)\_readmeui_with_src_%NOW3%.zip"

:results
echo.
echo ================================================================================
echo (REO) Results
echo ================================================================================
dir "_readmeui*.zip"

:done
echo.
